package eu.europa.ec.digit.leos.pilot.export.model.metadata;

import java.lang.IllegalArgumentException;

public enum MetadataLanguageDateFormat {

    BG("BG", "BUL","dd.M.yyyy"),
    CS("CS", "CES","dd.M.yyyy"),
    DA("DA", "DAN","dd.M.yyyy"),
    DE("DE", "DEU","dd.M.yyyy"),
    EL("EL", "ELL","dd.M.yyyy"),
    EN("EN", "ENG","dd.M.yyyy"),
    ES("ES", "SPA","dd.M.yyyy"),
    ET("ET", "EST","dd.M.yyyy"),
    FI("FI", "FIN","dd.M.yyyy"),
    FR("FR", "FRA","dd.M.yyyy"),
    GA("GA", "GLE","dd.M.yyyy"),
    HR("HR", "HRV","dd.M.yyyy."),
    HU("HU", "HUN","yyyy.M.dd."),
    IT("IT", "ITA","dd.M.yyyy"),
    LT("LT", "LIT","yyyy MM dd"),
    LV("LV", "LAV","dd.M.yyyy"),
    MT("MT", "MLT","dd.M.yyyy"),
    NL("NL", "NLD","dd.M.yyyy"),
    PL("PL", "POL","dd.M.yyyy"),
    PT("PT", "POR","dd.M.yyyy"),
    RO("RO", "RON","dd.M.yyyy"),
    SK("SK", "SLK","dd. M. yyyy"),
    SL("SL", "SLV","dd.M.yyyy"),
    SV("SV", "SWE","dd.M.yyyy");

    private final String format;

    private final String iso639_1;

    private final String iso639_2t;

    private MetadataLanguageDateFormat(String iso639_1, String iso639_2t, String format){
        this.iso639_1 = iso639_1;
        this.iso639_2t = iso639_2t;
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public String getIso639_1() {
        return iso639_1;
    }

    public String getIso639_2t() {
        return iso639_2t;
    }

    @Override
    public String toString(){
        return String.format("MetadataLanguageDateFormat(ISO-639-1: %s / ISO-639-2T: %s / Format: %s)",
                this.iso639_1, this.iso639_2t, this.format);
    }

    public static MetadataLanguageDateFormat ofIso639_2T(String value) throws IllegalArgumentException {
        if (value != null) {
            if (MetadataLanguageDateFormat.BG.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.BG;
            }
            if (MetadataLanguageDateFormat.CS.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.CS;
            }
            if (MetadataLanguageDateFormat.DA.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.DA;
            }
            if (MetadataLanguageDateFormat.DE.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.DE;
            }
            if (MetadataLanguageDateFormat.EL.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.EL;
            }
            if (MetadataLanguageDateFormat.EN.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.EN;
            }
            if (MetadataLanguageDateFormat.ES.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.ES;
            }
            if (MetadataLanguageDateFormat.ET.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.ET;
            }
            if (MetadataLanguageDateFormat.FI.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.FI;
            }
            if (MetadataLanguageDateFormat.FR.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.FR;
            }
            if (MetadataLanguageDateFormat.GA.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.GA;
            }
            if (MetadataLanguageDateFormat.HR.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.HR;
            }
            if (MetadataLanguageDateFormat.HU.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.HU;
            }
            if (MetadataLanguageDateFormat.IT.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.IT;
            }
            if (MetadataLanguageDateFormat.LT.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.LT;
            }
            if (MetadataLanguageDateFormat.LV.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.LV;
            }
            if (MetadataLanguageDateFormat.MT.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.MT;
            }
            if (MetadataLanguageDateFormat.NL.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.NL;
            }
            if (MetadataLanguageDateFormat.PL.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.PL;
            }
            if (MetadataLanguageDateFormat.PT.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.PT;
            }
            if (MetadataLanguageDateFormat.RO.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.RO;
            }
            if (MetadataLanguageDateFormat.SK.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.SK;
            }
            if (MetadataLanguageDateFormat.SL.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.SL;
            }
            if (MetadataLanguageDateFormat.SV.getIso639_2t().equals(value)){
                return MetadataLanguageDateFormat.SV;
            }
        }
        throw new IllegalArgumentException();
    }

    public static MetadataLanguageDateFormat ofIso639_1(String value) throws IllegalArgumentException {
        if (value != null) {
            if (MetadataLanguageDateFormat.BG.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.BG;
            }
            if (MetadataLanguageDateFormat.CS.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.CS;
            }
            if (MetadataLanguageDateFormat.DA.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.DA;
            }
            if (MetadataLanguageDateFormat.DE.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.DE;
            }
            if (MetadataLanguageDateFormat.EL.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.EL;
            }
            if (MetadataLanguageDateFormat.EN.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.EN;
            }
            if (MetadataLanguageDateFormat.ES.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.ES;
            }
            if (MetadataLanguageDateFormat.ET.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.ET;
            }
            if (MetadataLanguageDateFormat.FI.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.FI;
            }
            if (MetadataLanguageDateFormat.FR.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.FR;
            }
            if (MetadataLanguageDateFormat.GA.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.GA;
            }
            if (MetadataLanguageDateFormat.HR.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.HR;
            }
            if (MetadataLanguageDateFormat.HU.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.HU;
            }
            if (MetadataLanguageDateFormat.IT.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.IT;
            }
            if (MetadataLanguageDateFormat.LT.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.LT;
            }
            if (MetadataLanguageDateFormat.LV.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.LV;
            }
            if (MetadataLanguageDateFormat.MT.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.MT;
            }
            if (MetadataLanguageDateFormat.NL.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.NL;
            }
            if (MetadataLanguageDateFormat.PL.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.PL;
            }
            if (MetadataLanguageDateFormat.PT.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.PT;
            }
            if (MetadataLanguageDateFormat.RO.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.RO;
            }
            if (MetadataLanguageDateFormat.SK.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.SK;
            }
            if (MetadataLanguageDateFormat.SL.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.SL;
            }
            if (MetadataLanguageDateFormat.SV.getIso639_1().equals(value)){
                return MetadataLanguageDateFormat.SV;
            }
        }
        throw new IllegalArgumentException();
    }
}