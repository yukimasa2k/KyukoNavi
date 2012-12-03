package jp.thicklab.kyukonavi;

public class ItemBean {
	public static String modifytext(String s) {
		StringBuffer sb = new StringBuffer(s);
		String str = "";
		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);
			if (c >= 'ａ' && c <= 'ｚ') {
				sb.setCharAt(i, (char) (c - 'ａ' + 'a'));
			} else if (c >= 'Ａ' && c <= 'Ｚ') {
				sb.setCharAt(i, (char) (c - 'Ａ' + 'A'));
			} else if (c >= '０' && c <= '９') {
				sb.setCharAt(i, (char) (c - '０' + '0'));
			} else if (c == '　') {
				sb.setCharAt(i, ' ');
			} else if (c == '．') {
				sb.setCharAt(i, '.');
			} else if (c == '，') {
				sb.setCharAt(i, ',');
			} else if (c == '－') {
				sb.setCharAt(i, '-');
			} else if (c == '（') {
				sb.setCharAt(i, '(');
			} else if (c == '）') {
				sb.setCharAt(i, ')');
			}
		}
		str = sb.toString();
		//複数spc削除
		str = str.replaceAll("　", " ");
		str = str.trim();
		str = str.replaceAll(" {2,}", " ");
		str = str.replaceAll("&nbsp;","");

		return str;
	}
	public String K_Time = "";
	public String K_name = "";
	public String Reason = "";
	public String T_name = "";
	private String txtPlace = "";
	private String txtName = "";
	private String txtSeason = "";
	private String txtDay = "";
	private String txtTime = "";
	private String txtData1 = "";
	private String txtData2 = "";

	public void setK_Time(String K_Time){
		this.K_Time = K_Time;
	}
	public String getK_Time(){
		return K_Time;
	}
	public void setK_name(String K_name){
		this.K_name = modifytext(K_name);
	}
	public String getK_name(){
		return K_name;
	}
	public void setReason(String Reason){
		this.Reason = modifytext(Reason);
	}
	public String getReason(){
		return Reason;
	}
	public void setT_name(String T_name){
		this.T_name = modifytext(T_name);
	}
	public String getT_name(){
		return T_name;
	}
	public void setPlace(String Place){
		this.txtPlace = Place;
	}
	public String getPlace(){
		return txtPlace;
	}

	public void setName(String Name){
		this.txtName = modifytext(Name);
	}
	public String getName(){
		return txtName;
	}

	public void setSeason(String Season){
		this.txtSeason = Season;
	}
	public String getSeason(){
		return txtSeason;
	}

	public void setDay(String Day){
		this.txtDay = Day;
	}
	public String getDay(){
		return txtDay;
	}

	public void setTime(String Time){
		this.txtTime = Time;
	}
	public String getTime(){
		return txtTime;
	}

	public void setData1(String Data1){
		this.txtData1 = Data1;
	}
	public String getData1(){
		return txtData1;
	}

	public void setData2(String Data2){
		this.txtData2 = Data2;
	}
	public String getData2(){
		return txtData2;
	}

}

