package com.ibm.ServerWizard2;

import java.io.Writer;

import org.w3c.dom.Element;

public class AttributeValueNative extends AttributeValue {
	private Field field;

	public AttributeValueNative(Attribute a) {
		super(a);
		field = new Field();
		field.attributeName=a.name;
		field.desc=a.desc;
		fields.add(field);
	}
	public AttributeValueNative(AttributeValueNative a) {
		super(a);
		field=fields.get(0);
	}
	public void readXML(Element e) {
		field.value = SystemModel.getElement(e, "default");
		field.name = SystemModel.getElement(e, "name");
		field.readonly = this.readonly;
	}

	public void readInstanceXML(Element e) {
		field.value = SystemModel.getElement(e, "default");
	}

	@Override
	public void writeInstanceXML(Writer out) throws Exception {
		out.write("\t\t<default>" + field.value + "</default>\n");
	}

	@Override
	public String getValue() {
		return field.value;
	}

	@Override
	public String toString() {
		return "default(" + field.name + ")";
	}

	@Override
	public void setValue(String value) {
		field.value = value;
	}

	@Override
	public Boolean isEmpty() {
		return field.value.isEmpty();
	}

	@Override
	public void setValue(AttributeValue value) {
		AttributeValueNative n = (AttributeValueNative) value;
		field.value = n.field.value;
		field.name = n.field.name;
	}
/*
	@Override
	public Control getEditor(Table table, AttributeTableItem item) {
		Text text = new Text(table, SWT.NONE);
		text.setData(item);
		text.setText(value);
		text.setSelection(text.getText().length());
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text text = (Text) e.getSource();
				AttributeTableItem a = (AttributeTableItem) text.getData();
				AttributeValueSimple v = (AttributeValueSimple) a.getAttribute();
				v.value = text.getText();
				a.getItem().setText(2, v.value);
			}
		});
		return text;
	}
	*/
}
