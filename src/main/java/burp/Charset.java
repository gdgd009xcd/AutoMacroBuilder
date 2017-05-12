package burp;



public enum Charset  {
	UTF8("UTF-8"),
	EUCJP("EUC-JP"),
	SJIS("SHIFT_JIS"),
	ISO8859("ISO-8859-1"),
	USASCII("US-ASCII"),
	WINDOWS31J("WINDOWS-31J"),
	ISO2022JP("ISO-2022-JP");

	private final String name;

	//コンストラクタ
    Charset(String _name) {
        this.name = _name.toUpperCase();
    }

    public String GetName() {
        return this.name;
    }

    public static Charset getEnum(String str) {
        // enum型全てを取得します。
        Charset[] enumArray = Charset.values();

        // 取得出来たenum型分ループします。
        for(Charset enumStr : enumArray) {
            // 引数とenum型の文字列部分を比較します。
            if (str.toUpperCase().equals(enumStr.name.toString())){
                return enumStr;
            }
        }
        return null;
    }
}


