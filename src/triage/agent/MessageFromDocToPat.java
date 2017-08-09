package triage.agent;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

public class MessageFromDocToPat implements Savable{


	private static final long serialVersionUID = 1L;

	ERPatientAgent erPAgent;		// 患者エージェント。
	double lfConsultationTime;		// 診察に要した時間。
	double lfOperationTime;			// 手術に要した時間。
	int iDoctorDepartment;			// 医師所属室
	int iEmergencyLevel;			// 緊急度
	double lfExamAISHead;			// 検査結果頭部AIS
	double lfExamAISFace;			// 検査結果顔面AIS
	double lfExamAISNeck;			// 検査結果頸部AIS
	double lfExamAISThorax;			// 検査結果胸部AIS
	double lfExamAISAbdomen;		// 検査結果腹部AIS
	double lfExamAISSpine;			// 検査結果脊椎AIS
	double lfExamAISUpperExtremity;	// 検査結果上肢AIS
	double lfExamAISLowerExtremity;	// 検査結果上肢AIS
	double lfExamAISUnspecified;	// 検査結果体表・熱傷・その他外傷AIS

	/**
	 * <PRE>
	 *   コンストラクタ
	 * </PRE>
	 */
	MessageFromDocToPat()
	{
		erPAgent 					= null;		// 患者エージェント。
		lfConsultationTime			= 0;		// 診察に要した時間。
		lfOperationTime				= 0;		// 手術に要した時間。
		iDoctorDepartment			= 0;		// 医師の担当部門
		iEmergencyLevel				= 0;		// 緊急度
		lfExamAISHead				= 0.0;		// 検査結果頭部AIS
		lfExamAISFace 				= 0.0;		// 検査結果顔面AIS
		lfExamAISNeck				= 0.0;		// 検査結果頸部AIS
		lfExamAISThorax 			= 0.0;		// 検査結果胸部AIS
		lfExamAISAbdomen 			= 0.0;		// 検査結果腹部AIS
		lfExamAISSpine 				= 0.0;		// 検査結果脊椎AIS
		lfExamAISUpperExtremity 	= 0.0;		// 検査結果上肢AIS
		lfExamAISLowerExtremity 	= 0.0;		// 検査結果上肢AIS
		lfExamAISUnspecified 		= 0.0;		// 検査結果体表・熱傷・その他外傷AIS
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
	 * @param lfConsultationTimeData 診察時間
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
	 * @param lfOperationTimeData 手術時間
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
	 * @since 2015/08/05
	 * @version 0.1
	 */
	public double lfGetOperationTime()
	{
		return lfOperationTime;
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
