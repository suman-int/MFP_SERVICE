package com.mnao.mfp.cr.converter;

public interface AttributeConverter<X, Y> {
	public Y convertToDatabaseColumn(X attributeObject);

	public X convertToEntityAttribute(Y dbData);
}
