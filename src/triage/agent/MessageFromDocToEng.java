package triage.agent;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;


// エージェント間のメッセージ送信用に必要。
public class MessageFromDocToEng implements Savable{

	private static final long serialVersionUID = 8554343849007941046L;

	private ERPatientAgent erPAgent;		// 患者エージェント。
	private double lfConsultationTime;		// 診察に要した時間。
	private int iClinicalEngineerDepartment;// 依頼された技士の担当部門
	private int iDoctorDepartment;			// 医師の担当部門
	private int iRequestExamination;		// 医師から依頼された検査を表す変数。
	private int aiRequestAnatomy[];			// 医師から依頼された検査をする部位。
	private int iRequestExaminationNum;		// 医師から依頼された検査をする箇所
	private int iEmergencyLevel;			// 医師から得た患者の緊急度

	/**
	 * <PRE>
	 *    コンストラクタ
	 * </PRE>
	 */
	MessageFromDocToEng()
	{
		erPAgent 					= null;		// 患者エージェント。
		lfConsultationTime			= 0;		// 診察に要した時間。
		iClinicalEngineerDepartment	= 0;		// 依頼された技士の担当部門
		iDoctorDepartment			= 0;		// 医師の担当部門
		iRequestExamination			= 0;		// 医師から依頼された検査を表す変数。
		aiRequestAnatomy			= null;		// 医師から依頼された検査をする部位。
		iRequestExaminationNum		= 0;		// 医師から依頼された検査をする箇所
		iEmergencyLevel				= 0;		// 医師から得た患者の緊急度
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
	 *    依頼された担当技士の所属している部門を設定します。
	 * </PRE>
	 * @param iDepartmentChargeData 技士の担当部門
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
	 *    医師が依頼する検査を設定します。
	 * </PRE>
	 * @param iRequestExaminationData
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
	 * @param aiRequestAnatomyData	検査する部位
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public void vSetRequestAnatomy( int aiRequestAnatomyData[] )
	{
		int i;
		if( aiRequestAnatomyData != null )
		{
			aiRequestAnatomy = new int[aiRequestAnatomyData.length];
			for( i = 0;i < aiRequestAnatomyData.length; i++ )
			{
				aiRequestAnatomy[i] = aiRequestAnatomyData[i];
			}
		}
	}

	/**
	 * <PRE>
	 *    医師が依頼する検査する部位数を設定します。
	 * </PRE>
	 * @param iRequestExaminationNumData	検査する部位数
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
	 *    医師からの患者の緊急度情報を設定します。
	 * </PRE>
	 * @param iEmergencyLevelData
	 * @author kobayashi
	 * @since 2015/08/05
	 * @version 0.1
	 */
	public void vSetEmergencyLevel( int iEmergencyLevelData )
	{
		iEmergencyLevel = iEmergencyLevelData;
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
	 *    医師が依頼する検査を取得します。
	 * </PRE>
	 * @return 検査部位
	 * @author kobayashi
	 * @since 2015/07/29
	 * @version 0.1
	 */
	public int iGetRequestExamination()
	{
		return iRequestExamination;
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
	public int[] aiGetRequestAnatomys()
	{
		return aiRequestAnatomy;
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
	 *    医師から得た緊急度情報を取得します。
	 * </PRE>
	 * @return
	 */
	public int iGetEmergencyLevel()
	{
		return iEmergencyLevel;
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
