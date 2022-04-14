package com.mnao.mfp.cr.converter;

import javax.persistence.Converter;

//@Converter(autoApply=true)
public class IsActiveConverter implements AttributeConverter<Boolean, String>{

	@Override
	public String convertToDatabaseColumn(Boolean attributeObject) {
		return attributeObject ? "Y" : "N";
	}

	@Override
	public Boolean convertToEntityAttribute(String dbData) {
		return dbData != null && dbData.equalsIgnoreCase("Y") ? Boolean.TRUE : Boolean.FALSE;
	}

}
