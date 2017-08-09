package triage.agent;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

public class MessageFromDocToDoc implements Savable
{
	private static final long serialVersionUID = 3475746159892942226L;

	ERPatientAgent erPAgent;			// 患者エージェント
	double lfConsultationTime;			// 診察に要した時間
	double lfOperationTime;				// 手術に要した時間
	int iFromDoctorDepartment;			// 送信元医師の担当部門
	int iToDoctorDepartment;			// 送信先医師の担当部門
	int iEmergencyLevel;				// 緊急度
	double lfJudgedAISHead;				// 検査結果頭部AIS
	double lfJudgedAISFace;				// 検査結果顔面AIS
	double lfJudgedAISNeck;				// 検査結果頸部AIS
	double lfJudgedAISThorax;			// 検査結果胸部AIS
	double lfJudgedAISAbdomen;			// 検査結果腹部AIS
	double lfJudgedAISSpine;			// 検査結果脊椎AIS
	double lfJudgedAISUpperExtremity;	// 検査結果上肢AIS
	double lfJudgedAISLowerExtremity;	// 検査結果上肢AIS
	double lfJudgedAISUnspecified;		// 検査結果体表・熱傷・その他外傷AIS

	/**
	 * <PRE>
	 *   コンストラクタ
	 * </PRE>
	 */
	MessageFromDocToDoc()
	{
		erPAgent 					= null;		// 患者エージェント。
		lfConsultationTime			= 0;		// 診察に要した時間。
		iFromDoctorDepartment		= 0;		// 送信元医師の担当部門
		iToDoctorDepartment			= 0;		// 送信先医師の担当部門
		iEmergencyLevel				= 0;		// 緊急度
		lfJudgedAISHead				= 0.0;		// 検査結果頭部AIS
		lfJudgedAISFace 			= 0.0;		// 検査結果顔面AIS
		lfJudgedAISNeck				= 0.0;		// 検査結果頸部AIS
		lfJudgedAISThorax 			= 0.0;		// 検査結果胸部AIS
		lfJudgedAISAbdomen 			= 0.0;		// 検査結果腹部AIS
		lfJudgedAISSpine 			= 0.0;		// 検査結果脊椎AIS
		lfJudgedAISUpperExtremity 	= 0.0;		// 検査結果上肢AIS
		lfJudgedAISLowerExtremity 	= 0.0;		// 検査結果上肢AIS
		lfJudgedAISUnspecified 		= 0.0;		// 検査結果体表・熱傷・その他外傷AIS
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
	 *    医師が診察した時間を設定します。
	 * </PRE>
	 * @param lfConsultationTimeData 検査時間
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetConsultationTime( double lfConsultationTimeData )
	{
		lfConsultationTime = lfConsultationTimeData;
	}

	/**
	 * <PRE>
	 *    医師が手術した時間を設定します。
	 * </PRE>
	 * @param lfOperationTimeData 検査時間
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetOperationTime( double lfOperationTimeData )
	{
		lfOperationTime = lfOperationTimeData;
	}

	/**
	 * <PRE>
	 *    送信元医師の所属している部門を設定します。
	 * </PRE>
	 * @param iDepartmentChargeData 医師の担当部門
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetFromDoctorDepartment( int iDepartmentChargeData )
	{
		iFromDoctorDepartment = iDepartmentChargeData;
	}

	/**
	 * <PRE>
	 *    送信先医師の所属している部門を設定します。
	 * </PRE>
	 * @param iDepartmentChargeData 看護師の担当部門
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetToDoctorDepartment( int iDepartmentChargeData )
	{
		iToDoctorDepartment = iDepartmentChargeData;
	}

	/**
	 * <PRE>
	 *    緊急度を設定します。
	 * </PRE>
	 * @param iEmergency
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetEmergencyLevel( int iEmergency )
	{
		iEmergencyLevel = iEmergency;
	}

	/**
	 * <PRE>
	 *    頭部AIS値を設定します。
	 * </PRE>
	 * @param lfJudgedAISHeadData 頭部AIS値
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetJudgedAISHead( double lfJudgedAISHeadData )
	{
		lfJudgedAISHead = lfJudgedAISHeadData;
	}

	/**
	 *
	 * <PRE>
	 *    顔面AIS値を設定します。
	 * </PRE>
	 * @param lfJudgedAISFaceData 顔面AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetJudgedAISFace( double lfJudgedAISFaceData )
	{
		lfJudgedAISFace = lfJudgedAISFaceData;
	}

	/**
	 * <PRE>
	 *    頸部AIS値を設定します。
	 * </PRE>
	 * @param lfJudgedAISNeckData 頸部AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetJudgedAISNeck( double lfJudgedAISNeckData )
	{
		lfJudgedAISNeck = lfJudgedAISNeckData;
	}

	/**
	 * <PRE>
	 *    胸部AIS値を設定します。
	 * </PRE>
	 * @param lfJudgedAISThoraxData 胸部AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetJudgedAISThorax( double lfJudgedAISThoraxData )
	{
		lfJudgedAISThorax = lfJudgedAISThoraxData;
	}

	/**
	 * <PRE>
	 *    腹部AIS値を設定します。
	 * </PRE>
	 * @param lfJudgedAISAbdomenData 腹部AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetJudgedAISAbdomen( double lfJudgedAISAbdomenData )
	{
		lfJudgedAISAbdomen = lfJudgedAISAbdomenData;
	}

	/**
	 * <PRE>
	 *    脊椎AIS値を設定します。
	 * </PRE>
	 * @param lfJudgedAISSpineData 脊椎AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetJudgedAISSpine( double lfJudgedAISSpineData )
	{
		lfJudgedAISSpine = lfJudgedAISSpineData;
	}

	/**
	 * <PRE>
	 *    上肢AIS値を設定します。
	 * </PRE>
	 * @param lfJudgedAISUpperExtremityData 上肢AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetJudgedAISUpperExtremity( double lfJudgedAISUpperExtremityData )
	{
		lfJudgedAISUpperExtremity = lfJudgedAISUpperExtremityData;
	}

	/**
	 * <PRE>
	 *    下肢AIS値を設定します。
	 * </PRE>
	 * @param lfJudgedAISLowerExtremityData 下肢AIS値(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetJudgedAISLowerExtremity( double lfJudgedAISLowerExtremityData )
	{
		lfJudgedAISLowerExtremity = lfJudgedAISLowerExtremityData;
	}

	/**
	 * <PRE>
	 *    表面、熱傷、その他外傷AIS値を設定します。
	 * </PRE>
	 * @param lfJudgedAISUnspecifiedData 表面、熱傷、その他外傷(1～6)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetJudgedAISUnspecified( double lfJudgedAISUnspecifiedData )
	{
		lfJudgedAISUnspecified = lfJudgedAISUnspecifiedData;
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
	 *    医師が診察した時間を取得します。
	 * </PRE>
	 * @return 医師が診察した時間
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public double lfGetConsultationTime()
	{
		return lfConsultationTime;
	}

	/**
	 * <PRE>
	 *    医師が手術した時間を取得します。
	 * </PRE>
	 * @return 医師が手術した時間
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public double lfGetOperationTime()
	{
		return lfOperationTime;
	}

	/**
	 * <PRE>
	 *    送信元医師の部門を取得します。
	 * </PRE>
	 * @return 送信元医師が所属している部門
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public int iGetFromDoctorDepartment()
	{
		return iFromDoctorDepartment;
	}

	/**
	 * <PRE>
	 *    送信先医師の所属部門を取得します。
	 * </PRE>
	 * @return 送信先医師が所属している部門
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public int iGetToDoctorDepartment()
	{
		return iToDoctorDepartment;
	}

	/**
	 * <PRE>
	 *    緊急度を取得します。
	 * </PRE>
	 * @return 緊急度(1～5)
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public int iGetEmergencyLevel()
	{
		return iEmergencyLevel;
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
		return lfJudgedAISHead;
	}

	/**
	 * <PRE>
	 *   顔面のAISを取得します。
	 * </PRE>
	 * @return 顔面AISを返却。
	 */
	double lfGetAISFace()
	{
		return lfJudgedAISFace;
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
		return lfJudgedAISNeck;
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
		return lfJudgedAISThorax;
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
		return lfJudgedAISAbdomen;
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
		return lfJudgedAISSpine;
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
		return lfJudgedAISUpperExtremity;
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
		return lfJudgedAISLowerExtremity;
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
		return lfJudgedAISUnspecified;
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

}
