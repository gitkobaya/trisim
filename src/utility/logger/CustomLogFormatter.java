package utility.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class CustomLogFormatter extends SimpleFormatter
{
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * <PRE>
	 *    ユーザ指定のログフォーマットを記述します。
	 * </PRE>
	 * @param logRecord ログ出力のフォーマットが記載されたLogRecordクラス
	 * @return 実際のログ出力結果（文字列）
	 */
	public String format( LogRecord logRecord)
	{
		final StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(this.dateFormat.format(new Date(logRecord.getMillis())));
		stringBuffer.append(" ");

		Level level = logRecord.getLevel();
		if( level == Level.FINEST )
		{
			stringBuffer.append("FINEST");
		}
		else if( level == Level.FINER )
		{
			stringBuffer.append("FINER");
		}
		else if( level == Level.FINE )
		{
			stringBuffer.append("FINE");
		}
		else if( level == Level.CONFIG )
		{
			stringBuffer.append("CONFIG");
		}
		else if( level == Level.INFO )
		{
			stringBuffer.append("INFO");
		}
		else if( level == Level.WARNING )
		{
			stringBuffer.append("WARNING");
		}
		else if( level == Level.SEVERE )
		{
			stringBuffer.append("SEVERE");
		}
		else
		{
			stringBuffer.append(Integer.toString(logRecord.getLevel().intValue()));
			stringBuffer.append(" ");
		}

		stringBuffer.append(" ");
		stringBuffer.append(logRecord.getLoggerName());
		stringBuffer.append(" - ");
		stringBuffer.append( logRecord.getMessage());
		stringBuffer.append("\n");

		return stringBuffer.toString();
	}
}
