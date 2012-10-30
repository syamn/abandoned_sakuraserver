/**
 * sakuraserver - Package: syam.util
 * Created: 2012/10/30 19:23:02
 */
package syam.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Util (Util.java)
 * @author syam(syamn)
 */
public class Util {
	/**
	 * 文字列が整数型に変換できるか返す
	 * @param str チェックする文字列
	 * @return 変換成功ならtrue、失敗ならfalse
	 */
	public static boolean isInteger(String str) {
		try{
			Integer.parseInt(str);
		}catch (NumberFormatException e){
			return false;
		}
		return true;
	}

	/**
	 * 文字列がdouble型に変換できるか返す
	 * @param str チェックする文字列
	 * @return 変換成功ならtrue、失敗ならfalse
	 */
	public static boolean isDouble(String str) {
		try{
			Double.parseDouble(str);
		}catch (NumberFormatException e){
			return false;
		}
		return true;
	}

	/**
	 * PHPの join(array, delimiter) と同じ関数
	 * @param s 結合するコレクション
	 * @param delimiter デリミタ文字
	 * @return 結合後の文字列
	 */
	public static String join(Collection<?> s, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		Iterator<?> iter = s.iterator();

		// 要素が無くなるまでループ
		while (iter.hasNext()){
			buffer.append(iter.next());
			// 次の要素があればデリミタを挟む
			if (iter.hasNext()){
				buffer.append(delimiter);
			}
		}
		// バッファ文字列を返す
		return buffer.toString();
	}

	/**
	 * ファイル名から拡張子を返します
	 * @param fileName ファイル名
	 * @return ファイルの拡張子
	 */
	public static String getSuffix(String fileName) {
		if (fileName == null)
			return null;
		int point = fileName.lastIndexOf(".");
		if (point != -1) {
			return fileName.substring(point + 1);
		}
		return fileName;
	}

	/**
	 * Unix秒を yy/MM/dd HH:mm:ss フォーマットにして返す
	 * @param unixSec Unix秒
	 * @return yy/MM/dd HH:mm:ss
	 */
	public static String getDispTimeByUnixTime(long unixSec){
		SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
		return sdf.format(new Date(unixSec * 1000));
	}

	/**
	 * 現在のUnix秒を取得する
	 * @return long unixSec
	 */
	public static Long getCurrentUnixSec(){
		return System.currentTimeMillis() / 1000;
	}

	/**
	 * Unix秒からDateを取得して返す
	 * @return Date
	 */
	public static Date getDateByUnixTime(long unixSec){
		return new Date(unixSec * 1000);
	}

	/**
	 * 分を読みやすい時間表記にして返す
	 * @param min
	 * @return
	 */
	public static String getReadableTimeByMinute(int min){
		if (min < 0) return "0分間";
		if (min < 60) return min + "分間";
		if (min % 60 == 0) return min / 60 + "時間";

		int h = min / 60;
		int m = min % 60;
		return h + "時間" + m + "分";
	}

	/**
	 * 秒の差を読みやすい時間表記にして返す
	 * @param before beforeSec
	 * @param after afterSec
	 * @return string
	 */
	public static String getDiffString(Long before, Long after){
		boolean minus = false;
		long diffSec = after - before;
		if (diffSec == 0){
			return "0秒";
		}
		else if (diffSec < 0){
			minus = true;
			diffSec = -diffSec;
		}
		String ret = "";

		final int SEC = 1;
		final int MIN = SEC * 60;
		final int HOUR = MIN * 60;
		final int DAY = HOUR * 24;

		if ((diffSec / DAY) >= 1){
			ret += diffSec / DAY + "日";
			diffSec = diffSec - ((diffSec / DAY) * DAY);
		}
		if ((diffSec / HOUR) >= 1){
			ret += diffSec / HOUR + "時間";
			diffSec = diffSec - ((diffSec / HOUR) * HOUR);
		}
		if ((diffSec / MIN) >= 1){
			ret += diffSec / MIN + "分";
			diffSec = diffSec - ((diffSec / MIN) * MIN);
		}
		if ((diffSec / SEC) >= 1){
			ret += diffSec / SEC + "秒";
			diffSec = diffSec - ((diffSec / SEC) * SEC);
		}

		if (minus){
			ret = "-" + ret;
		}
		return ret;
	}

	/**
	 * 文字列からCalendarクラスの単位数値を返す
	 * @param str 文字列
	 * @return 対応する数値 または変換出来ない場合 -1
	 */
	public static int getMeasure(String str){
		int measure = 0;

		if (str.equalsIgnoreCase("SECOND")){
			measure = Calendar.SECOND;
		}else if (str.equalsIgnoreCase("MINUTE")){
			measure = Calendar.MINUTE;
		}else if(str.equalsIgnoreCase("HOUR")){
			measure = Calendar.HOUR;
		}else if(str.equalsIgnoreCase("DAY")){
			measure = Calendar.DAY_OF_MONTH;
		}else if(str.equalsIgnoreCase("WEEK")){
			measure = Calendar.WEEK_OF_MONTH;
		}else if(str.equalsIgnoreCase("MONTH")){
			measure = Calendar.MONTH;
		}else if(str.equalsIgnoreCase("YEAR")){
			measure = Calendar.YEAR;
		}else{
			measure = -1;
		}

		return measure;
	}
}
