package ambuilder;

//https://docs.oracle.com/javase/jp/1.5.0/guide/intl/encoding.doc.html

public enum Encode  {
	ISO_8859_1("ISO-8859-1"),
	ISO_8859_2("ISO-8859-2"),
	ISO_8859_4("ISO-8859-4"),
	ISO_8859_5("ISO-8859-5"),
	ISO_8859_7("ISO-8859-7"),
	ISO_8859_9("ISO-8859-9"),
	ISO_8859_13("ISO-8859-13"),
	ISO_8859_15("ISO-8859-15"),
	KOI8_R("KOI8-R"),
	US_ASCII("US-ASCII"),
	UTF_8("UTF-8"),
	UTF_16("UTF-16"),
	UTF_16BE("UTF-16BE"),
	UTF_16LE("UTF-16LE"),
	windows_1250("windows-1250"),
	windows_1251("windows-1251"),
	windows_1252("windows-1252"),
	windows_1253("windows-1253"),
	windows_1254("windows-1254"),
	windows_1257("windows-1257"),
	Big5("Big5"),
	Big5_HKSCS("Big5-HKSCS"),
	EUC_JP("EUC-JP"),
	EUC_KR("EUC-KR"),
	GB18030("GB18030"),
	GB2312("GB2312"),
	GBK("GBK"),
	IBM_Thai("IBM-Thai"),
	IBM00858("IBM00858"),
	IBM01140("IBM01140"),
	IBM01141("IBM01141"),
	IBM01142("IBM01142"),
	IBM01143("IBM01143"),
	IBM01144("IBM01144"),
	IBM01145("IBM01145"),
	IBM01146("IBM01146"),
	IBM01147("IBM01147"),
	IBM01148("IBM01148"),
	IBM01149("IBM01149"),
	IBM037("IBM037"),
	IBM1026("IBM1026"),
	IBM1047("IBM1047"),
	IBM273("IBM273"),
	IBM277("IBM277"),
	IBM278("IBM278"),
	IBM280("IBM280"),
	IBM284("IBM284"),
	IBM285("IBM285"),
	IBM297("IBM297"),
	IBM420("IBM420"),
	IBM424("IBM424"),
	IBM437("IBM437"),
	IBM500("IBM500"),
	IBM775("IBM775"),
	IBM850("IBM850"),
	IBM852("IBM852"),
	IBM855("IBM855"),
	IBM857("IBM857"),
	IBM860("IBM860"),
	IBM861("IBM861"),
	IBM862("IBM862"),
	IBM863("IBM863"),
	IBM864("IBM864"),
	IBM865("IBM865"),
	IBM866("IBM866"),
	IBM868("IBM868"),
	IBM869("IBM869"),
	IBM870("IBM870"),
	IBM871("IBM871"),
	IBM918("IBM918"),
	ISO_2022_CN("ISO-2022-CN"),
	ISO_2022_JP("ISO-2022-JP"),
	ISO_2022_KR("ISO-2022-KR"),
	ISO_8859_3("ISO-8859-3"),
	ISO_8859_6("ISO-8859-6"),
	ISO_8859_8("ISO-8859-8"),
	Shift_JIS("Shift_JIS"),
	TIS_620("TIS-620"),
	windows_1255("windows-1255"),
	windows_1256("windows-1256"),
	windows_1258("windows-1258"),
	windows_31j("windows-31j"),
	x_Big5_Solaris("x-Big5_Solaris"),
	x_euc_jp_linux("x-euc-jp-linux"),
	x_EUC_TW("x-EUC-TW"),
	x_eucJP_Open("x-eucJP-Open"),
	x_IBM1006("x-IBM1006"),
	x_IBM1025("x-IBM1025"),
	x_IBM1046("x-IBM1046"),
	x_IBM1097("x-IBM1097"),
	x_IBM1098("x-IBM1098"),
	x_IBM1112("x-IBM1112"),
	x_IBM1122("x-IBM1122"),
	x_IBM1123("x-IBM1123"),
	x_IBM1124("x-IBM1124"),
	x_IBM1381("x-IBM1381"),
	x_IBM1383("x-IBM1383"),
	x_IBM33722("x-IBM33722"),
	x_IBM737("x-IBM737"),
	x_IBM856("x-IBM856"),
	x_IBM874("x-IBM874"),
	x_IBM875("x-IBM875"),
	x_IBM921("x-IBM921"),
	x_IBM922("x-IBM922"),
	x_IBM930("x-IBM930"),
	x_IBM933("x-IBM933"),
	x_IBM935("x-IBM935"),
	x_IBM937("x-IBM937"),
	x_IBM939("x-IBM939"),
	x_IBM942("x-IBM942"),
	x_IBM942C("x-IBM942C"),
	x_IBM943("x-IBM943"),
	x_IBM943C("x-IBM943C"),
	x_IBM948("x-IBM948"),
	x_IBM949("x-IBM949"),
	x_IBM949C("x-IBM949C"),
	x_IBM950("x-IBM950"),
	x_IBM964("x-IBM964"),
	x_IBM970("x-IBM970"),
	x_ISCII91("x-ISCII91"),
	x_ISO2022_CN_CNS("x-ISO2022-CN-CNS"),
	x_ISO2022_CN_GB("x-ISO2022-CN-GB"),
	x_iso_8859_11("x-iso-8859-11"),
	x_JISAutoDetect("x-JISAutoDetect"),
	x_Johab("x-Johab"),
	x_MacArabic("x-MacArabic"),
	x_MacCentralEurope("x-MacCentralEurope"),
	x_MacCroatian("x-MacCroatian"),
	x_MacCyrillic("x-MacCyrillic"),
	x_MacDingbat("x-MacDingbat"),
	x_MacGreek("x-MacGreek"),
	x_MacHebrew("x-MacHebrew"),
	x_MacIceland("x-MacIceland"),
	x_MacRoman("x-MacRoman"),
	x_MacRomania("x-MacRomania"),
	x_MacSymbol("x-MacSymbol"),
	x_MacThai("x-MacThai"),
	x_MacTurkish("x-MacTurkish"),
	x_MacUkraine("x-MacUkraine"),
	x_MS950_HKSCS("x-MS950-HKSCS"),
	x_mswin_936("x-mswin-936"),
	x_PCK("x-PCK"),
	x_windows_874("x-windows-874"),
	x_windows_949("x-windows-949"),
	x_windows_950("x-windows-950");


	private final String name;
	private final String uppercasename;

	//コンストラクタ
    Encode(String _name) {
        this.name = _name;
        this.uppercasename = _name.toUpperCase();
    }

    public String getIANACharset() {
        return this.name;
    }

    public static String[] getIANAlist(){
        Encode[] enumArray = Encode.values();
        String[] ianalist = new String[enumArray.length];
        int i = 0;
        for(Encode enumval : enumArray){
            ianalist[i] = new String(enumval.getIANACharset());
        }
        return ianalist;
    }
    
    public static Encode getEnum(String str) {
        // enum型全てを取得します。
        Encode[] enumArray = Encode.values();

        // 取得出来たenum型分ループします。
        for(Encode enumStr : enumArray) {
            // 引数とenum型の文字列部分を比較します。
            if (str.toUpperCase().equals(enumStr.uppercasename.toString())){
                return enumStr;
            }
        }
        return Encode.UTF_8;//default
    }
    
    public static boolean isExistEnc(String str){
        // enum型全てを取得します。
        Encode[] enumArray = Encode.values();

        // 取得出来たenum型分ループします。
        for(Encode enumStr : enumArray) {
            // 引数とenum型の文字列部分を比較します。
            if (str.toUpperCase().equals(enumStr.uppercasename.toString())){
                return true;
            }
        }
        return false;//default
    }
}


