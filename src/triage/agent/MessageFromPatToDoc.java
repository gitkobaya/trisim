package triage.agent;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

public class MessageFromPatToDoc implements Savable{

	private static final long serialVersionUID = 1L;

	ERPatientAgent erPAgent;					// 患者エージェント。
	double lfObservationTime;					// 観察に要した時間。
	int iDoctorDepartment;						// 医師の担当部門
	double lfWaitTime;							// 患者の待ち時間
	double lfExamAISHead;						// 検査結果頭部AIS
	double lfExamAISFace;						// 検査結果顔面AIS
	double lfExamAISNeck;						// 検査結果頸部AIS
	double lfExamAISThorax;						// 検査結果胸部AIS
	double lfExamAISAbdomen;					// 検査結果腹部AIS
	double lfExamAISSpine;						// 検査結果脊椎AIS
	double lfExamAISUpperExtremity;				// 検査結果上肢AIS
	double lfExamAISLowerExtremity;				// 検査結果上肢AIS
	double lfExamAISUnspecified;				// 検査結果体表・熱傷・その他外傷AIS
	String strInjuryHeadStatus;					// 文字による頭部AIS
	String strInjuryFaceStatus;					// 文字による顔面AIS
	String strInjuryNeckStatus;					// 文字による頸部AIS
	String strInjuryThoraxStatus;				// 文字による胸部AIS
	String strInjuryAbdomenStatus;				// 文字による腹部AIS
	String strInjurySpineStatus;				// 文字による脊椎AIS
	String strInjuryUpperExtremityStatus;		// 文字による上肢AIS
	String strInjuryLowerExtremityStatus;		// 文字による下肢AIS
	String strInjuryUnspecifiedStatus;			// 文字による体表・熱傷・その他外傷AIS
	int iSurvivalFlag;							// 患者生存フラグ

	/**
	 * <PRE>
	 *   コンストラクタ
	 * </PRE>
	 */
	MessageFromPatToDoc()
	{
		erPAgent 					= null;		// 患者エージェント。
		lfObservationTime			= 0;		// 観察に要した時間。
		iDoctorDepartment			= 0;		// 医師の担当部門
		lfWaitTime					= 0.0;		// 患者の待ち時間
		lfExamAISHead				= 0.0;		// 検査結果頭部AIS
		lfExamAISFace 				= 0.0;		// 検査結果顔面AIS
		lfExamAISNeck				= 0.0;		// 検査結果頸部AIS
		lfExamAISThorax 			= 0.0;		// 検査結果胸部AIS
		lfExamAISAbdomen 			= 0.0;		// 検査結果腹部AIS
		lfExamAISSpine 				= 0.0;		// 検査結果脊椎AIS
		lfExamAISUpperExtremity 	= 0.0;		// 検査結果上肢AIS
		lfExamAISLowerExtremity 	= 0.0;		// 検査結果上肢AIS
		lfExamAISUnspecified 		= 0.0;		// 検査結果体表・熱傷・その他外傷AIS
		strInjuryHeadStatus			= "";		// 文字による頭部AIS
		strInjuryFaceStatus			= "";		// 文字による顔面AIS
		strInjuryNeckStatus			= "";		// 文字による頸部AIS
		strInjuryThoraxStatus			= "";		// 文字による胸部AIS
		strInjuryAbdomenStatus			= "";		// 文字による腹部AIS
		strInjurySpineStatus			= "";		// 文字による脊椎AIS
		strInjuryUpperExtremityStatus	= "";		// 文字による上肢AIS
		strInjuryLowerExtremityStatus	= "";		// 文字による下肢AIS
		strInjuryUnspecifiedStatus		= "";		// 文字による体表・熱傷・その他外傷AIS
		iSurvivalFlag					= 0;
	}

	/**
	 * <PRE>
	 *    医師が受け持っている患者オブジェクトを設定します。
	 * </PRE>
	 * @param erPAgentData 患者オブジェクト
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetERPatientAgent( ERPatientAgent erPAgentData )
	{
		erPAgent = erPAgentData;
	}

	/**
	 * <PRE>
	 *    看護師が観察した時間を設定します。
	 * </PRE>
	 * @param lfObservationTimeData 観察時間
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetObservationTime( double lfObservationTimeData )
	{
		lfObservationTime = lfObservationTimeData;
	}

	/**
	 * <PRE>
	 *    医師の所属している部門を設定します。
	 * </PRE>
	 * @param iDepartmentChargeData 医師の担当部門
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetDoctorDepartment( int iDepartmentChargeData )
	{
		iDoctorDepartment = iDepartmentChargeData;
	}

	/**
	 * <PRE>
	 *    今まで待った待ち時間を設定します。
	 * </PRE>
	 * @param lfWaitTimeData 患者の総待ち時間
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetWaitTime( double lfWaitTimeData )
	{
		lfWaitTime = lfWaitTimeData;
	}

	/**
	 * <PRE>
	 *    頭部AIS値を設定します。
	 * </PRE>
	 * @param lfExamAISHeadData 頭部AIS値
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetExamAISHead( double lfExamAISHeadData )
	{
		lfExamAISHead = lfExamAISHeadData;
	}

	/**
	 *
	 * <PRE>
	 *    顔面AIS値を設定します。
	 * </PRE>
	 * @param lfExamAISFaceData 顔面AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetExamAISFace( double lfExamAISFaceData )
	{
		lfExamAISFace = lfExamAISFaceData;
	}

	/**
	 * <PRE>
	 *    頸部AIS値を設定します。
	 * </PRE>
	 * @param lfExamAISNeckData 頸部AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetExamAISNeck( double lfExamAISNeckData )
	{
		lfExamAISNeck = lfExamAISNeckData;
	}

	/**
	 * <PRE>
	 *    胸部AIS値を設定します。
	 * </PRE>
	 * @param lfExamAISThoraxData 胸部AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetExamAISThorax( double lfExamAISThoraxData )
	{
		lfExamAISThorax = lfExamAISThoraxData;
	}

	/**
	 * <PRE>
	 *    腹部AIS値を設定します。
	 * </PRE>
	 * @param lfExamAISAbdomenData 腹部AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetExamAISAbdomen( double lfExamAISAbdomenData )
	{
		lfExamAISAbdomen = lfExamAISAbdomenData;
	}

	/**
	 * <PRE>
	 *    脊椎AIS値を設定します。
	 * </PRE>
	 * @param lfExamAISSpineData 脊椎AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetExamAISSpine( double lfExamAISSpineData )
	{
		lfExamAISSpine = lfExamAISSpineData;
	}

	/**
	 * <PRE>
	 *    上肢AIS値を設定します。
	 * </PRE>
	 * @param lfExamAISUpperExtremityData 上肢AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetExamAISUpperExtremity( double lfExamAISUpperExtremityData )
	{
		lfExamAISUpperExtremity = lfExamAISUpperExtremityData;
	}

	/**
	 * <PRE>
	 *    下肢AIS値を設定します。
	 * </PRE>
	 * @param lfExamAISLowerExtremityData 下肢AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetExamAISLowerExtremity( double lfExamAISLowerExtremityData )
	{
		lfExamAISLowerExtremity = lfExamAISLowerExtremityData;
	}

	/**
	 * <PRE>
	 *    表面、熱傷、その他外傷AIS値を設定します。
	 * </PRE>
	 * @param lfExamAISUnspecifiedData 表面、熱傷、その他外傷(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetExamAISUnspecified( double lfExamAISUnspecifiedData )
	{
		lfExamAISUnspecified = lfExamAISUnspecifiedData;
	}

	/**
	 * <PRE>
	 *    頭部AIS値の患者口頭状態を設定します。
	 * </PRE>
	 * @param strInjuryHeadStatusData 頭部AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetInjuryHeadStatus( String strInjuryHeadStatusData )
	{
		strInjuryHeadStatus = strInjuryHeadStatusData;
	}

	/**
	 *
	 * <PRE>
	 *    顔面AIS値を設定します。
	 * </PRE>
	 * @param strInjuryFaceStatusData 顔面AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetInjuryFaceStatus( String strInjuryFaceStatusData )
	{
		strInjuryFaceStatus = strInjuryFaceStatusData;
	}

	/**
	 * <PRE>
	 *    頸部AIS値を設定します。
	 * </PRE>
	 * @param strInjuryNeckStatusData 頸部AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetInjuryNeckStatus( String strInjuryNeckStatusData )
	{
		strInjuryNeckStatus = strInjuryNeckStatusData;
	}

	/**
	 * <PRE>
	 *    胸部AIS値を設定します。
	 * </PRE>
	 * @param strInjuryThoraxStatusData 胸部AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetInjuryThoraxStatus( String strInjuryThoraxStatusData )
	{
		strInjuryThoraxStatus = strInjuryThoraxStatusData;
	}

	/**
	 * <PRE>
	 *    腹部AIS値を設定します。
	 * </PRE>
	 * @param strInjuryAbdomenStatusData 腹部AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetInjuryAbdomenStatus( String strInjuryAbdomenStatusData )
	{
		strInjuryAbdomenStatus = strInjuryAbdomenStatusData;
	}

	/**
	 * <PRE>
	 *    脊椎AIS値を設定します。
	 * </PRE>
	 * @param strInjurySpineStatusData 脊椎AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetInjurySpineStatus( String strInjurySpineStatusData )
	{
		strInjurySpineStatus = strInjurySpineStatusData;
	}

	/**
	 * <PRE>
	 *    上肢AIS値を設定します。
	 * </PRE>
	 * @param strInjuryUpperExtremityDataStatus 上肢AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetInjuryUpperExtremityStatus( String strInjuryUpperExtremityDataStatus )
	{
		strInjuryUpperExtremityStatus = strInjuryUpperExtremityDataStatus;
	}

	/**
	 * <PRE>
	 *    下肢AIS値を設定します。
	 * </PRE>
	 * @param strInjuryLowerExtremityStatusData 下肢AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetInjuryLowerExtremityStatus( String strInjuryLowerExtremityStatusData )
	{
		strInjuryLowerExtremityStatus = strInjuryLowerExtremityStatusData;
	}

	/**
	 * <PRE>
	 *    表面、熱傷、その他外傷AIS値を設定します。
	 * </PRE>
	 * @param strInjuryUnspecifiedStatusData 表面、熱傷、その他外傷口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetInjuryUnspecifiedStatus( String strInjuryUnspecifiedStatusData )
	{
		strInjuryUnspecifiedStatus = strInjuryUnspecifiedStatusData;
	}

	/**
	 * <PRE>
	 *    患者が生存しているか否かを表すフラグを設定します。
	 * </PRE>
	 * @param iSurvivalFlagData 患者生存フラグ
	 *                          0 死亡
	 *                          1 生存
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetSurvivalFlag( int iSurvivalFlagData )
	{
		iSurvivalFlag = iSurvivalFlagData;
	}

	/**
	 * <PRE>
	 *    医師が受け持っている患者オブジェクトを取得します。
	 * </PRE>
	 * @return 患者オブジェクト
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public ERPatientAgent cGetERPatientAgent()
	{
		return erPAgent;
	}

	/**
	 * <PRE>
	 *    技士が検査した時間を取得します。
	 * </PRE>
	 * @return 技士が検査した時間
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public double lfGetObservationTime()
	{
		return lfObservationTime;
	}

	/**
	 * <PRE>
	 *    医師の部門を取得します。
	 * </PRE>
	 * @return 医師が所属している部門
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public int iGetDoctorDepartment()
	{
		return iDoctorDepartment;
	}

	/**
	 * <PRE>
	 *    今まで待った待ち時間を取得します。
	 * </PRE>
	 * @return 待ち時間
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public double lfGetWaitTime()
	{
		return lfWaitTime;
	}

	/**
	 * <PRE>
	 *   頭部のAISを取得します。
	 * </PRE>
	 * @return 頭部AISを返却。
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	double lfGetAISHead()
	{
		return lfExamAISHead;
	}

	/**
	 * <PRE>
	 *   顔面のAISを取得します。
	 * </PRE>
	 * @return 顔面AISを返却。
	 */
	double lfGetAISFace()
	{
		return lfExamAISFace;
	}

	/**
	 * <PRE>
	 *   首のAISを取得します。
	 * </PRE>
	 * @return 首AISを返却。
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	double lfGetAISNeck()
	{
		return lfExamAISNeck;
	}

	/**
	 * <PRE>
	 *   胸部のAISを取得します。
	 * </PRE>
	 * @return 胸部AISを返却。
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	double lfGetAISThorax()
	{
		return lfExamAISThorax;
	}

	/**
	 * <PRE>
	 *   腹部のAISを取得します。
	 * </PRE>
	 * @return 腹部AISを返却。
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	double lfGetAISAbdomen()
	{
		return lfExamAISAbdomen;
	}

	/**
	 * <PRE>
	 *   脊椎のAISを取得します。
	 * </PRE>
	 * @return 脊椎AISを返却。
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	double lfGetAISSpine()
	{
		return lfExamAISSpine;
	}

	/**
	 * <PRE>
	 *   上肢のAISを取得します。
	 * </PRE>
	 * @return 上肢AISを返却。
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	double lfGetAISUpperExtremity()
	{
		return lfExamAISUpperExtremity;
	}

	/**
	 * <PRE>
	 *   下肢のAISを取得します。
	 * </PRE>
	 * @return 下肢AISを返却。
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	double lfGetAISLowerExtremity()
	{
		return lfExamAISLowerExtremity;
	}

	/**
	 * <PRE>
	 *   表面・熱傷・その他外傷のAISを取得します。
	 * </PRE>
	 * @return 特定しないAISを返却。
	 * @author kobayashi
	 * @since 2015/07/29
	 */
	double lfGetAISUnspecified()
	{
		return lfExamAISUnspecified;
	}

	/**
	 * <PRE>
	 *    頭部AIS値の患者口頭状態を取得します。
	 * </PRE>
	 * @return 頭部AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public String strGetInjuryHeadStatus()
	{
		return strInjuryHeadStatus;
	}

	/**
	 *
	 * <PRE>
	 *    顔面AIS値を設定します。
	 * </PRE>
	 * @return 顔面AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public String strGetInjuryFaceStatus()
	{
		return strInjuryFaceStatus;
	}

	/**
	 * <PRE>
	 *    頸部AIS値を取得します。
	 * </PRE>
	 * @return 頸部AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public String strGetInjuryNeckStatus()
	{
		return strInjuryNeckStatus;
	}

	/**
	 * <PRE>
	 *    胸部AIS値を取得します。
	 * </PRE>
	 * @return 胸部AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public String strGetInjuryThoraxStatus()
	{
		return strInjuryThoraxStatus;
	}

	/**
	 * <PRE>
	 *    腹部AIS値を取得します。
	 * </PRE>
	 * @return 腹部AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public String strGetInjuryAbdomenStatus()
	{
		return strInjuryAbdomenStatus;
	}

	/**
	 * <PRE>
	 *    脊椎AIS値を取得します。
	 * </PRE>
	 * @return 脊椎AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public String strGetInjurySpineStatus()
	{
		return strInjurySpineStatus;
	}

	/**
	 * <PRE>
	 *    上肢AIS値を取得します。
	 * </PRE>
	 * @return 上肢AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public String strGetInjuryUpperExtremityStatus()
	{
		return strInjuryUpperExtremityStatus;
	}

	/**
	 * <PRE>
	 *    下肢AIS値を取得します。
	 * </PRE>
	 * @return 下肢AIS値口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public String strGetInjuryLowerExtremityStatus()
	{
		return strInjuryLowerExtremityStatus;
	}

	/**
	 * <PRE>
	 *    表面、熱傷、その他外傷AIS値を取得します。
	 * </PRE>
	 * @return 表面、熱傷、その他外傷口頭内容
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public String strGetInjuryUnspecifiedStatus()
	{
		return strInjuryUnspecifiedStatus;
	}

	/**
	 * <PRE>
	 *    患者が生存しているか否かを表すフラグを取得します。
	 * </PRE>
	 * @return 患者生存フラグ
	 *         0 死亡
	 *         1 生存
	 * @author kobayashi
	 * @since 2015/10/20
	 * @version 0.1
	 */
	public int iGetSurvivalFlag()
	{
		return iSurvivalFlag;
	}

	@Override
	public SaveDataPackage saveStatus() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Savable restoreStatus(SaveDataPackage saveData) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public int iGetPatientLocation() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

}
