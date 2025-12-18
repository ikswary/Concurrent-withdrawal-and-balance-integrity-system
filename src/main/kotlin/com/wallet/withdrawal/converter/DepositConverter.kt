package com.wallet.withdrawal.converter

import com.wallet.withdrawal.domain.vo.Money
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.math.BigDecimal

/**
 * JPA AttributeConverter for Money Value Object
 * Money <-> BigDecimal 변환
 */
@Converter(autoApply = true)
class DepositConverter : AttributeConverter<Money, BigDecimal> {

    override fun convertToDatabaseColumn(attribute: Money?): BigDecimal? {
        return attribute?.amount
    }

    override fun convertToEntityAttribute(dbData: BigDecimal?): Money? {
        return dbData?.let { Money(it) }
    }
}