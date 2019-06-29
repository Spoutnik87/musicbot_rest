package fr.spoutnik87.musicbot_rest.exception

class InvalidModelFIeldException(modelName: String, fieldName: String, value: Any) : IllegalArgumentException("Unable to set the field $fieldName in the model $modelName. Value : $value")