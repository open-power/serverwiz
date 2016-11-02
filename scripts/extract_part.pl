use strict;
use XML::Simple;
use XML::Parser;
use Data::Dumper;
    
my $filename = $ARGV[0];
my $partid = $ARGV[1];

if (scalar(@ARGV) < 2) {
	die "Usage: extract_part.pl [system xml file] [part id]\n";
}
    
$XML::Simple::PREFERRED_PARSER = 'XML::Parser';
print "Loading MRW XML: $filename\n";
my $xml =
   XMLin($filename,KeyAttr => ['-id'], forcearray => [ 'child_id', 'hidden_child_id', 'bus',
                                      'property', 'field', 'attribute', 'enumerator' ]);

my $lookup = {};
my $outfile = "";
foreach my $t (@{$xml->{'targetInstances'}{'targetInstance'}}) {
	$lookup->{$t->{'id'}} = $t;
	if ($t->{'id'} eq $partid) {
		$outfile = $t->{'type'}.".xml";
	}
}
if ($outfile eq "") { die "Part ID $partid not found\n"; }

print "Writing $outfile\n";
open(OUT,">$outfile") || die "Unable to create $outfile\n";
print OUT "<partInstance>
<version>2.1</version>
<targetInstances>
";

printPart($lookup,$partid);
print OUT "</targetInstances>
</partInstance>";

close OUT;

sub printPart {
	my $lookup = shift;
	my $id = shift;
	my $ptr = $lookup->{$id};
	print OUT XMLout($ptr,NoAttr => 1,RootName => 'targetPart');
	foreach my $child (@{$ptr->{'child_id'}}) {
		printPart($lookup,$child);
	}
}