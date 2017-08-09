package inverse.optimization.ga;


public class BaseException extends Throwable{

	private static final long serialVersionUID = -7330238142543020731L;

	private int iErrCode;
	private int iErrLine;
	private String strMethodName;
	private String strClassName;
	private String strModuleName;
	private String strErrDetail;

	public BaseException()
	{
		iErrCode = 0;
		strMethodName = "";
		strClassName = "";
		strErrDetail = "";
	}

	/*
	 * コンストラクタ(エラー???報を設定す???)
	 * @param iCode       エラーコー???
	 * @param sMethodName 関数???
	 * @param sClassName  クラス???
	 * @param sDetail     エラー詳細
	 * @author kobayashi
	 * @since 2009/6/14
	 * @version 1.0
	 */
	public BaseException(int iCode, String sMethodName, String sClassName, String sDetail)
	{
		iErrCode		= iCode;
		strMethodName	= sMethodName;
		strClassName	= sClassName;
		strErrDetail	= sDetail;
	}

	/*
	 * コンストラクタ(エラー???報を設定す???)
	 * @param iCode       エラーコー???
	 * @param iLine       行数
	 * @param sMethodName 関数???
	 * @param sClassName  クラス???
	 * @param sDetail     エラー詳細
	 * @author kobayashi
	 * @since 2009/6/14
	 * @version 1.0
	 */
	public BaseException(int iCode, String sMethodName, String sClassName, String sDetail, int iLine )
	{
		iErrCode		= iCode;
		iErrLine		= iLine;
		strMethodName	= sMethodName;
		strClassName	= sClassName;
		strErrDetail	= sDetail;
	}


	/*
	 * エラー???報を設定す???
	 * @param iCode       エラーコー???
	 * @param sMethodName 関数???
	 * @param sClassName  クラス???
	 * @param sDetail     エラー詳細
	 * @author kobayashi
	 * @since 2009/6/14
	 * @version 1.0
	 */
	public void SetErrorInfo(int iCode, String sMethodName, String sClassName, String sDetail)
	{
		iErrCode = iCode;
		strMethodName = sMethodName;
		strClassName = sClassName;
		strErrDetail = sDetail;
	}

	/*
	 * コンストラクタ(エラー???報を設定す???)
	 * @param iCode       エラーコー???
	 * @param iLine       行数
	 * @param sMethodName 関数???
	 * @param sClassName  クラス???
	 * @param sDetail     エラー詳細
	 * @author kobayashi
	 * @since 2009/6/14
	 * @version 1.0
	 */
	public void SetErrorInfo(int iCode, String sMethodName, String sClassName, String sDetail, int iLine )
	{
		iErrCode = iCode;
		iErrLine = iLine;
		strMethodName = sMethodName;
		strClassName = sClassName;
		strErrDetail = sDetail;
	}

	/*
	 * エラー番号を?????力す???
	 * @author kobayashi
	 * @since 2009/6/14
	 * @version 1.0
	 */
	public int iGetErrCode()
	{
		return iErrCode;
	}

	/*
	 * エラーを起こした行数を?????力す???
	 * @author kobayashi
	 * @since 2009/6/14
	 * @version 1.0
	 */
	public int iGetErrorLine()
	{
		return iErrLine;
	}

	/*
	 * エラーを起こした関数名を出力す???
	 * @author kobayashi
	 * @since 2009/6/14
	 * @version 1.0
	 */
	public String strGetMethodName()
	{
		return strMethodName;
	}

	/*
	 * エラーを起こしたクラスを?????力す???
	 * @author kobayashi
	 * @since 2009/6/14
	 * @version 1.0
	 */
	public String strGetClassName()
	{
		return strClassName;
	}

	/*
	 * エラーの詳細???報を?????力す???
	 * @author kobayashi
	 * @since 2009/6/14
	 * @version 1.0
	 */
	public String strGetErrDetail()
	{
		return strErrDetail;
	}

}
