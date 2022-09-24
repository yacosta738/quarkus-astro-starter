package com.acosta.quarkusastro.config

import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.ws.rs.ext.ParamConverter
import javax.ws.rs.ext.ParamConverterProvider
import javax.ws.rs.ext.Provider


@Provider
class LocalDateProvider : ParamConverterProvider {
    override fun <T> getConverter(
        rawType: Class<T>,
        genericType: Type,
        annotations: Array<Annotation>
    ): ParamConverter<T> {
        return object : ParamConverter<T> {
            override fun fromString(value: String): T {
                return LocalDate.parse(value, dateFormatter) as T
            }

            override fun toString(value: T): String {
                return (value as LocalDate).format(dateFormatter)
            }
        }
    }

    companion object {
        var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(Constants.LOCAL_DATE_FORMAT)
    }
}