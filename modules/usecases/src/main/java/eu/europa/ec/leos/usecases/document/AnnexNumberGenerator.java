package eu.europa.ec.leos.usecases.document;

import eu.europa.ec.leos.i18n.MessageHelper;
import org.apache.jena.sparql.util.RomanNumeral;

/**
 * This Class is responsible for generating the Annex Number.
 *
 */
public class AnnexNumberGenerator {

	/**
	 * @param Annex
	 *            number for conversion
	 * @return Roman Number
	 */
	public static String getAnnexNumber(int number, MessageHelper messageHelper) {
		try {
			if (number == 0) {
				return messageHelper.getMessage("document.annex.title.prefix");
			} else {
				return messageHelper.getMessage("document.annex.title.prefix") +" "+ RomanNumeral.asRomanNumerals(number);
			}
		} catch (NumberFormatException exception) {
			// If number of outside limit of 1-3999, decimal number will be returned
			return messageHelper.getMessage("document.annex.title.prefix") +" " +number;
		}
	}

}
