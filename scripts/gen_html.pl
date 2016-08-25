#! /usr/bin/perl
# IBM_PROLOG_BEGIN_TAG
# This is an automatically generated prolog.
#
# $Source: src/usr/targeting/common/processMrw.pl $
#
# OpenPOWER HostBoot Project
#
# Contributors Listed Below - COPYRIGHT 2015,2016
# [+] International Business Machines Corp.
#
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
# implied. See the License for the specific language governing
# permissions and limitations under the License.
#
# IBM_PROLOG_END_TAG

use strict;
use HTML::Table;
use XML::Simple;
use Data::Dumper;
use Targets;
use Math::BigInt;
use Getopt::Long;
use File::Basename;

my $VERSION = "1.0.0";

my $force          = 0;
my $serverwiz_file = "";
my $version        = 0;
my $debug          = 0;
my $report         = 0;
my $sdr_file       = "";
my $htmlfile       = "";

GetOptions(
	"f"   => \$force,             # numeric
	"x=s" => \$serverwiz_file,    # string
	"d"   => \$debug,
	"v"   => \$version,
	"o=s"   => \$htmlfile,
	"r"   => \$report,
  )                               # flag
  or printUsage();

if ( $version == 1 ) {
	die "\nprocessMrw.pl\tversion $VERSION\n";
}

if ( $serverwiz_file eq "" ) {
	printUsage();
}

$XML::Simple::PREFERRED_PARSER = 'XML::Parser';

my $targetObj = Targets->new;
if ( $force == 1 ) {
	$targetObj->{force} = 1;
}
if ( $debug == 1 ) {
	$targetObj->{debug} = 1;
}

$targetObj->setVersion($VERSION);
my $xmldir = dirname($serverwiz_file);
$targetObj->loadXML($serverwiz_file);

my $attribute_lookup = {
	'SOURCE' => {
		'FSICM' => ['FSI_PORT','FSI_ENGINE'],
		'FSIM' => ['FSI_PORT','FSI_ENGINE'],
		'GPIO' => ['DIRECTION','GPIO_TYPE'],
		'POWER' => ['RAIL_NAME'],
	},
	'DEST' => {
		'I2C'  => ['I2C_ADDRESS'],
		'POWER' => ['RAIL_NAME'],
	},

	'BUS' => {
		'I2C'  => ['I2C_ADDRESS','I2C_SPEED'],
	}
};


my %summary;
foreach my $target ( sort keys %{ $targetObj->getAllTargets() } ) {
	my $num_conns = $targetObj->getNumConnections($target);
	if ( $num_conns == 0 ) { next; }

	my $parent_chip = findChipTarget($targetObj,$target);
	my $parent_target = $targetObj->getTargetParent($target);
	my $parent_type = $targetObj->getTargetType($parent_target);
	my $bus_type = $targetObj->getBusType($target);
	my $source_target = $target;
	my $source_ptr = $targetObj->getTarget($target);
	
	$source_target=~s/$parent_chip\///;
	$summary{$bus_type}{$parent_chip}{'TYPE'} = $targetObj->getTargetType($parent_chip);
	$summary{$bus_type}{$parent_chip}{'SOURCE'}{$source_target}{'TYPE'} = $targetObj->getTargetType($target);
	$summary{$bus_type}{$parent_chip}{'SOURCE'}{$source_target}{'NAME'} = getPinName($targetObj,$target);
	for ( my $i = 0 ; $i < $num_conns ; $i++ ) {
		my $dest_target = $targetObj->getConnectionDestination( $target, $i );
		my $bus_target = $targetObj->getConnectionBus( $target, $i );
		my $dest_chip = findChipTarget($targetObj,$dest_target);
		my $dest_instance = getPinName($targetObj,$dest_target);
		
		$summary{$bus_type}{$parent_chip}{'SOURCE'}{$target}{'DEST'}{$dest_target}{'CHIP'} = $dest_chip;
		$summary{$bus_type}{$parent_chip}{'SOURCE'}{$target}{'DEST'}{$dest_target}{'NAME'} = $dest_instance;
		if (defined $attribute_lookup->{'SOURCE'}->{$bus_type}) {
			foreach my $attr (@{$attribute_lookup->{'SOURCE'}->{$bus_type}}) {
				if (!$targetObj->isBadAttribute($target,$attr)) {
					$summary{$bus_type}{$parent_chip}{'SOURCE'}{$target}{'DEST'}{$dest_target}{'ATTRIBUTES'}{$attr} = $targetObj->getAttribute($target,$attr);
				}
			}
		}	
		if (defined $attribute_lookup->{'DEST'}->{$bus_type}) {
			foreach my $attr (@{$attribute_lookup->{'DEST'}->{$bus_type}}) {
				if (!$targetObj->isBadAttribute($dest_target,$attr)) {
					$summary{$bus_type}{$parent_chip}{'SOURCE'}{$target}{'DEST'}{$dest_target}{'ATTRIBUTES'}{$attr} = $targetObj->getAttribute($dest_target,$attr);
				}
			}
		}
		if (defined $attribute_lookup->{'BUS'}->{$bus_type}) {
			foreach my $attr (@{$attribute_lookup->{'BUS'}->{$bus_type}}) {
				if (defined $source_ptr->{CONNECTION}->{BUS}->[$i]->{'bus_attribute'}->{$attr}->{'default'} &&
				       $source_ptr->{CONNECTION}->{BUS}->[$i]->{'bus_attribute'}->{$attr}->{'default'} ne "" &&
				       ref($source_ptr->{CONNECTION}->{BUS}->[$i]->{'bus_attribute'}->{$attr}->{'default'}) ne "HASH") {
					$summary{$bus_type}{$parent_chip}{'SOURCE'}{$source_target}{'DEST'}{$dest_target}{'ATTRIBUTES'}{$attr} = 
						"(b)".$source_ptr->{CONNECTION}->{BUS}->[$i]->{'bus_attribute'}->{$attr}->{default};
				}
			}
		}
	}
}

my $outfile = $htmlfile;
if ($outfile eq "") {
	$outfile = $serverwiz_file;
	$outfile =~ s/\.xml//;
	$outfile = $outfile.".html";
}
open(my $html,"> $outfile") || die "Unable to create $outfile\n";

print $html "<head><title>$outfile</title></head>\n";

print $html "<body>\n";

print $html "<a href=\"#targets\"><h2>Targets</h2></a>";
print $html "<a href=\"#connections\"><h2>Connections by Bus Type</h2></a>";
print $html "<hr>";
print $html "<h2 id=\"targets\">Targets:</h2>\n";
getHier($html,$targetObj,"/sys");
print $html "<ul>\n";

print $html "<h2 id=\"connections\">Connections by Bus Type:</h2>\n";
foreach my $bustype (sort(keys(%summary))) {
	print $html "<li><a href=\"#$bustype\">$bustype</a>\n";
}
print $html "</ul><hr>";

foreach my $bustype (sort(keys(%summary))) {
	print $html "<hr><p><h3 id=\"$bustype\">$bustype</h3>\n";
	
	foreach my $parent_chip (sort(keys(%{$summary{$bustype}}))) {
		print $html "<p>\n";
		my $table = new HTML::Table(-cols=>4,-rules=>'all',-border=>3,-width=>'1400',-spacing=>0,-padding=>10);
		
		my $parent_ptr = $summary{$bustype}{$parent_chip};
		my $chip = $parent_chip." (".$parent_ptr->{'TYPE'}.")";
		$table->addRow("<b>$chip</b>");
		$table->setCellColSpan($table->getTableRows(),1,4);
		foreach my $source (sort(keys(%{$parent_ptr->{'SOURCE'}}))) {
			my $source_ptr = $parent_ptr->{'SOURCE'}->{$source}{'DEST'};

			foreach my $dest (sort(keys(%{$source_ptr}))) {
				my $source_attr = $source_ptr->{$dest}{'ATTRIBUTES'};
				my $attrs = "<ul>";
				foreach my $attr (sort(keys(%{$source_attr}))) {
					$attrs = $attrs."<li>".$attr."=".$source_attr->{$attr}."\n";
				}
				$attrs = $attrs."</ul>";
				$table->addRow($source,$dest,$attrs);
			}						
		}
		print $html $table->getTable();
	}
	
}
print $html "</body>\n";
close $html;

print "HTML File Written: $outfile\n";

sub getHier {
	my $file = shift;
	my $targetObj = shift;
	my $target = shift;
	my $level = shift;
	$level++;

	my $cls = $targetObj->getAttribute($target,"CLASS");	
	if ($cls eq "UNIT" || $cls eq "") { return; }
	my @parts = split(/\//,$target);
	my $inst = $parts[scalar(@parts)-1];
	my $tg = "";
	my $etg = "";
	if ($cls eq "CONNECTOR") { $tg="<i>"; $etg = "</i>"}
	if ($cls eq "CARD") { $tg="<b>"; $etg = "</b>"}
	my $type = $targetObj->getTargetType($target);
	print $file "<li>$tg$type : $inst$etg</li>\n";
	my $children = $targetObj->getTargetChildren($target);
	if (defined $children) {
		print $file "<ul>\n";
		foreach my $c (@{$children}) {
			getHier($file,$targetObj,$c,$level);
		}
		print $file "</ul>\n";
	}
}

sub getPinName { 
	my $targetObj = shift;
	my $target = shift;
	
	my @line = split(/\./,$targetObj->getInstanceName($target));
	my $i  = scalar(@line);
	return $line[$i-1];
}
sub findChipTarget {
	my $targetObj = shift;
	my $target = shift;

	my $cls = "";
	my $parent = "";
	while ($cls ne "CHIP" && defined $parent && $cls ne "LED" && $cls ne "CARD") {
		$parent = $targetObj->getTargetParent($target);
		if (defined $parent && $parent ne "") {
			if ($targetObj->isBadAttribute($parent,"CLASS")) {
				$cls = $targetObj->getAttribute( $parent, "MRW_TYPE" );
			} else { 
				$cls = $targetObj->getAttribute( $parent, "CLASS" );
			}
		}
		$target = $parent;
	}
	return $parent;
}