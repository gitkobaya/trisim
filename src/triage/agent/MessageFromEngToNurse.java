package triage.agent;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;


// エージェント間のメッセージ送信用に必要。
public class MessageFromEngToNurse implements Savable
{
	private static final long serialVersionUID = -917495446130401644L;

	ERPatientAgent erPAgent;		// 患者エージェント。
	double lfExaminationTime;		// 検査に要した時間。
	int iRequestExamination;		// 医師から依頼された検査を表す変数。
	int aiRequestAnatomys[];		// 医師から依頼された検査をする部位。
	int iRequestExaminationNum;		// 医師から依頼された検査部位数
	int iClinicalEngineerDepartment;// 医療技師所属室
	int iNurseDepartment;			// 看護師所属部門(医師と同一)
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
	MessageFromEngToNurse()
	{
		erPAgent 					= null;		// 患者エージェント。
		lfExaminationTime			= 0;		// 検査に要した時間。
		iClinicalEngineerDepartment	= 0;		// 医療技師の担当部門
		iRequestExamination			= 0;		// 医師から依頼された検査を表す変数。
		aiRequestAnatomys			= null;		// 医師から依頼された検査をする部位。
		iRequestExaminationNum		= 0;		// 医師から依頼された検査をする箇所
		lfExamAISHead				= 0.0;		// 検査結果頭部AIS
		lfExamAISFace 				= 0.0;		// 検査結果顔面AIS
		lfExamAISNeck				= 0.0;		// 検査結果頸部AIS
		lfExamAISThorax 			= 0.0;		// 検査結果胸部AIS
		lfExamAISAbdomen 			= 0.0;		// 検査結果腹部AIS
		lfExamAISSpine 				= 0.0;		// 検査結果脊椎AIS
		lfExamAISUpperExtremity 	= 0.0;		// 検査結果上肢AIS
		lfExamAISLowerExtremity 	= 0.0;		// 検査結果上肢AIS
		lfExamAISUnspecified 		= 0.0;		// 検査結果体表・熱傷・その他外傷AIS
		iNurseDepartment			= 0;		// 看護師の所属部署
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
	 *    技士が検査した時間を設定します。
	 * </PRE>
	 * @param lfExamTimeData 検査時間
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetExaminationTime( double lfExamTimeData )
	{
		lfExaminationTime = lfExamTimeData;
	}

	/**
	 * <PRE>
	 *    依頼された担当技士の所属している部門を設定します。
	 * </PRE>
	 * @param iDepartmentChargeData 技士の担当部門
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetClinicalEngineerDepartment( int iDepartmentChargeData )
	{
		iClinicalEngineerDepartment = iDepartmentChargeData;
	}

	/**
	 * <PRE>
	 *    依頼した部門を設定します。
	 * </PRE>
	 * @param iDepartmentChargeData 看護師の所属部門
	 * @author kobayashi
	 * @since 2015/08/07
	 * @version 0.1
	 */
	public void vSetNurseDepartment( int iDepartmentChargeData )
	{
		iNurseDepartment = iDepartmentChargeData;
	}

	/**
	 * <PRE>
	 *    医師が依頼する検査を設定します。
	 * </PRE>
	 * @param iRequestExaminationData 依頼する検査方法
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetRequestExamination( int iRequestExaminationData )
	{
		iRequestExamination = iRequestExaminationData;
	}

	/**
	 * <PRE>
	 *    医師が依頼する検査部位を設定します。
	 * </PRE>
	 * @param aiRequestAnatomysData	検査位置を格納した配列
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetRequestAnatomys( int[] aiRequestAnatomysData )
	{
		aiRequestAnatomys = aiRequestAnatomysData;
	}

	/**
	 * <PRE>
	 *    医師が依頼する検査する部位数を設定します。
	 * </PRE>
	 * @param iRequestExaminationNumData	検査部位数
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetRequestExaminationNum( int iRequestExaminationNumData )
	{
		iRequestExaminationNum = iRequestExaminationNumData;
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
	 *    技士が検査した時間を取得します。
	 * </PRE>
	 * @return 技士が検査した時間
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public double lfGetExaminationTime()
	{
		return lfExaminationTime;
	}

	/**
	 * <PRE>
	 *    依頼する技士の部門を取得します。
	 * </PRE>
	 * @return 依頼する技士が所属している部門
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public int iGetClinicalEngineerDepartment()
	{
		return iClinicalEngineerDepartment;
	}

	/**
	 * <PRE>
	 *    看護師の部門を取得します。（医師と同一部門）
	 * </PRE>
	 * @return 看護師が所属している部門（医師と同一部門）
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public int iGetNurseDepartment()
	{
		return iNurseDepartment;
	}

	/**
	 * <PRE>
	 *    医師が依頼する検査部位を設定します。
	 * </PRE>
	 * @return 検査部位
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public int[] iGetRequestAnatomys()
	{
		return aiRequestAnatomys;
	}

	/**
	 * <PRE>
	 *    医師が依頼する検査する部位数を返却します。
	 * </PRE>
	 * @return 依頼する検査部位数
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public int iGetRequestExaminationNum()
	{
		return iRequestExaminationNum;
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
