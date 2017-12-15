package triage.room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import triage.agent.ERDoctorAgent;
import triage.agent.ERDoctorAgentException;
import triage.agent.ERNurseAgent;
import triage.agent.ERPatientAgent;
import utility.initparam.InitSimParam;
import utility.node.ERTriageNode;
import utility.node.ERTriageNodeManager;
import utility.sfmt.Rand;

/**
 * 病院の高度治療室(HCU)を表すクラスです。
 * このプログラムではこのクラスを含めすべての部屋をエージェントとして定義しています。<br>
 * そのようにすることにより、いろいろと都合がよいためそのようにしております。<br>
 * HCUでは重症度が一般病棟よりも高くICUよりも低い患者が入院する病棟です。<br>
 * 日本外傷データバンク及び厚生労働省が出している統計を元に入院日数を算出し、その日までに
 * 所定の閾値まで下がるようにし、下がったら退院するような流れとなっています。<br>
 * 次に行く場所は以下の通りです。
 * １一般病棟<br>
 * ２ICU<br>
 * ５手術室<br>
 *
 * 使用方法は次の通りです。<br>
 * 初期化　　　　　　vInitialize　<br>
 * エージェント作成　vCreateDoctorAgents<br>
 * 　　　　　　　　　vCreateNurseAgents<br>
 * 設定　　　　　　　vSetDoctorAgentParameter<br>
 * 　　　　　　　　　vSetNurseAgentParameter<br>
 * 　　　　　　　　　vSetReadWriteFileForAgents<br>
 * 入院　　　　　　　vImplementHighCareUnitRoom<br>
 * 実行　　　　　　　action　<br>
 * 終了処理　　　　　　vTerminate　<br>
 *
 * @author kobayashi
 *
 */
public class ERHighCareUnitRoom extends Agent{

	private static final long serialVersionUID = -2991497552250616486L;

	Rand rnd;											// 乱数クラス
	ERDoctorAgent erHighCareUnitDoctorAgent;			// 高度治療室を担当している医師エージェント
	ArrayList<ERDoctorAgent> ArrayListDoctorAgents;		// 高度治療室を担当している医師エージェント
	ArrayList<ERNurseAgent> ArrayListNurseAgents;		// 高度治療室を担当している看護師エージェント
	ArrayList<ERPatientAgent> ArrayListPatientAgents;	// 高度治療室で入院しているエージェント
	ArrayList<Integer> ArrayListNursePatientLoc;		// 看護師と患者の対応位置
	double lfTotalTime;									// シミュレーション経過時間
	int iHospitalBedNum;								// 病床数
	int iAttachedDoctorNum;
	int iAttachedNurseNum;
	double lfHcuAvgLengthOfStay;						// 高度治療室の平均在院日数
	private Logger cHighCareUnitLog;					// 高度治療室のログ設定

	private ERTriageNodeManager erTriageNodeManager;
	private ERTriageNode erTriageNode;
	private int iInverseSimFlag;

	// 描画関係
	private int iDrawX;
	private int iDrawY;
	private int iDrawZ;
	private int iDrawCenterX;
	private int iDrawCenterY;
	private int iDrawWidth;
	private int iDrawHeight;
	private int iDrawF;

	private Object csHighCareUnitCriticalSection;		// クリティカルセクション用

	private double lfLastBedTime;
	private double lfLongestTotalTime;

	/**
	 * <PRE>
	 *    高度治療室のコンストラクタ
	 * </PRE>
	 */
	public ERHighCareUnitRoom()
	{
		vInitialize();
	}

	/**
	 * <PRE>
	 *    高度治療室のコンストラクタ
	 * </PRE>
	 * @param iAttachedDoctorNumData			所属医師数
	 * @param iAttachedNurseNumData				所属看護師数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERHighCareUnitRoom(int iAttachedDoctorNumData, int iAttachedNurseNumData )
	{
		vInitialize( iAttachedDoctorNumData, iAttachedNurseNumData );
	}

	/**
	 * <PRE>
	 *    高度治療室の初期設定を実行します。
	 * </PRE>
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vInitialize()
	{
		ArrayListDoctorAgents	= new ArrayList<ERDoctorAgent>();
		ArrayListNurseAgents	= new ArrayList<ERNurseAgent>();
		ArrayListPatientAgents	= new ArrayList<ERPatientAgent>();
		ArrayListNursePatientLoc= new ArrayList<Integer>();			// 看護師と患者の対応位置
		lfTotalTime				= 0;								// シミュレーション経過時間
		iHospitalBedNum			= 0;								// 病床数
		iAttachedDoctorNum		= 0;								// 所属している医師の数
		iAttachedNurseNum		= 0;								// 所属している看護師の数
		lfHcuAvgLengthOfStay	= 0;								// 高度治療室の平均在院日数

//		long seed;
//		seed = System.currentTimeMillis();
//		rnd = null;
//		rnd = new Sfmt( (int)seed );

		iInverseSimFlag = 0;

	}

	/**
	 * <PRE>
	 *    ファイルの読み込みを行います。
	 * </PRE>
	 * @param strDoctorAgentDirectory			医師エージェントの出力用ディレクトリパス
	 * @param strNurseAgentDirectory			看護師エージェントの出力用ディレクトリパス
	 * @param strClinicalEngineerAgentDirectory	医療技師エージェントの出力用ディレクトリパス
	 * @param iFileWriteMode					ファイル書き込みモード
	 * 											0 1ステップごとのデータを書き込み
	 * 											1 最初と最後各100ステップ程度のデータを書き込み
	 * @throws IOException						ファイル書き込みエラー
	 */
	public void vSetReadWriteFileForAgents( String strDoctorAgentDirectory, String strNurseAgentDirectory, String strClinicalEngineerAgentDirectory, int iFileWriteMode ) throws IOException
	{
		int i;

		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
		{
			ArrayListDoctorAgents.get(i).vSetReadWriteFile( iFileWriteMode );
		}
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetReadWriteFile( iFileWriteMode );
		}
	}

	/**
	 * <PRE>
	 *    初療室の初期設定を実行します。
	 * </PRE>
	 * @param iAttachedDoctorNumData			所属医師数
	 * @param iAttachedNurseNumData				所属看護師数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public void vInitialize( int iAttachedDoctorNumData, int iAttachedNurseNumData )
	{
		int i;
		iAttachedDoctorNum = iAttachedDoctorNumData;
		iAttachedNurseNum = iAttachedNurseNumData;

		ArrayListDoctorAgents			= new ArrayList<ERDoctorAgent>();
		ArrayListNurseAgents			= new ArrayList<ERNurseAgent>();
		for( i = 0;i < iAttachedDoctorNum; i++ )
		{
			ArrayListDoctorAgents.add( new ERDoctorAgent() );
		}
		erHighCareUnitDoctorAgent = ArrayListDoctorAgents.get(0);
		for( i = 0;i < iAttachedNurseNum; i++ )
		{
			ArrayListNurseAgents.add( new ERNurseAgent() );
			ArrayListNursePatientLoc.add( new Integer( -1 ) );
		}

		iInverseSimFlag = 0;
	}

	/**
	 * <PRE>
	 *    終了処理を実行します。
	 * </PRE>
	 * @throws IOException java標準のIOExceptionクラス
	 */
	public synchronized void vTerminate() throws IOException
	{
		int i;

		synchronized( csHighCareUnitCriticalSection )
		{
			// 患者エージェントの終了処理を行います。
			if( ArrayListPatientAgents != null )
			{
				for( i = ArrayListPatientAgents.size()-1; i >= 0; i-- )
				{
					if( ArrayListPatientAgents.get(i) != null )
					{
						ArrayListPatientAgents.get(i).vTerminate();
						this.getEngine().addExitAgent( ArrayListPatientAgents.get(i) );
						ArrayListPatientAgents.set( i, null );
						ArrayListPatientAgents.remove( i );
					}
				}
				ArrayListPatientAgents = null;
			}

			// 医師エージェントの終了処理を行います。
			if( ArrayListDoctorAgents != null )
			{
				for( i = ArrayListDoctorAgents.size()-1; i >= 0; i-- )
				{
					if( ArrayListDoctorAgents.get(i) != null )
					{
						ArrayListDoctorAgents.get(i).vTerminate();
						this.getEngine().addExitAgent( ArrayListDoctorAgents.get(i) );
						ArrayListDoctorAgents.set( i, null );
						ArrayListDoctorAgents.remove( i );
					}
				}
				ArrayListDoctorAgents = null;
			}

			if( ArrayListNurseAgents != null )
			{
				for( i = ArrayListNurseAgents.size()-1; i >= 0; i-- )
				{
					if( ArrayListNurseAgents.get(i) != null )
					{
						ArrayListNurseAgents.get(i).vTerminate();
						this.getEngine().addExitAgent( ArrayListNurseAgents.get(i) );
						ArrayListNurseAgents.set( i, null );
						ArrayListNurseAgents.remove( i );
					}
				}
				ArrayListNurseAgents = null;
			}

			if( ArrayListNursePatientLoc != null )
			{
				// 看護師エージェントの終了処理を行います。
				for( i = ArrayListNursePatientLoc.size()-1; i >= 0; i-- )
				{
					if( ArrayListNursePatientLoc.get(i) != null )
					{
						ArrayListNursePatientLoc.set( i, null );
						ArrayListNursePatientLoc.remove( i );
					}
				}
				ArrayListNursePatientLoc = null;
			}
			cHighCareUnitLog = null;								// 高度治療室ログ出力設定

			// 乱数
			rnd = null;												// 乱数クラス

			// FUSEノード、リンク
			erTriageNodeManager = null;
			erTriageNode = null;
			lfTotalTime = 0.0;
		}
	}

	/**
	 * <PRE>
	 *   初療室の医師エージェントを生成します。
	 * </PRE>
	 * @param iDoctorAgentNum  医師エージェント数
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vCreateDoctorAgents( int iDoctorAgentNum )
	{
		int i;
		// 0の場合は所属する部屋が存在しないため、医師を配置しないように設定します。
		if( ArrayListDoctorAgents == null )
		{
			// 逆シミュレーションの場合に通ります。
			ArrayListDoctorAgents = new ArrayList<ERDoctorAgent>();
		}
		for( i = 0;i < iDoctorAgentNum; i++ )
		{
		ArrayListDoctorAgents.add( new ERDoctorAgent() );
		}
		if( iDoctorAgentNum > 0 )
		{
			erHighCareUnitDoctorAgent = ArrayListDoctorAgents.get(0);
		}
	}

	/**
	 * <PRE>
	 *   初療室の看護師エージェントを生成します。
	 * </PRE>
	 * @param iNurseAgentNum 看護師エージェント数
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	public void vCreateNurseAgents( int iNurseAgentNum )
	{
		int i;
		// 0の場合は所属する部屋が存在しないため、看護師を配置しないように設定します。
		if( ArrayListNurseAgents == null )
		{
			// 逆シミュレーションの場合に通ります。
			ArrayListNurseAgents 		= new ArrayList<ERNurseAgent>();
			ArrayListNursePatientLoc	= new ArrayList<Integer>();		// 看護師と患者の対応位置
		}
		for( i = 0;i < iNurseAgentNum; i++ )
		{
			ArrayListNurseAgents.add( new ERNurseAgent() );
			ArrayListNursePatientLoc.add( new Integer( -1 ) );
		}
	}

	/**
	 * <PRE>
	 *    HCUの医師エージェントのパラメータを設定します。
	 * </PRE>
	 * @param alfYearExperience			経験年数
	 * @param alfConExperience			経験数の重み
	 * @param alfExperienceRate1		経験年数パラメータ1
	 * @param alfExperienceRate2		経験年数パラメータ2
	 * @param alfConExperienceAIS		経験年数重み（重症度）
	 * @param alfExperienceRateAIS1		経験年数パラメータその１（重症度）
	 * @param alfExperienceRateAIS2		経験年数パラメータその２（重症度）
	 * @param alfConTired1				疲労度パラメータ1
	 * @param alfConTired2				疲労度パラメータ2
	 * @param alfConTired3				疲労度パラメータ3
	 * @param alfConTired4				疲労度パラメータ4
	 * @param alfTiredRate				疲労度重み
	 * @param alfRevisedOperationRate	手術室改善度割合
	 * @param alfAssociationRate		関連性パラメータ
	 * @param alfConsultationTime		診察時間
	 * @param alfOperationTime			手術時間
	 * @param alfEmergencyTime			初療室処置時間
	 * @param aiDepartment				所属部署
	 * @param aiRoomNumber				所属部屋番号
	 * @author kobayashi
	 * @since 2015/08/10
	 * version 0.2
	 */
	public void vSetDoctorAgentParameter( double[] alfYearExperience,
										  double[] alfConExperience,
										  double[] alfExperienceRate1,
										  double[] alfExperienceRate2,
										  double[] alfConExperienceAIS,
										  double[] alfExperienceRateAIS1,
										  double[] alfExperienceRateAIS2,
										  double[] alfConTired1,
										  double[] alfConTired2,
										  double[] alfConTired3,
										  double[] alfConTired4,
										  double[] alfTiredRate,
										  double[] alfRevisedOperationRate,
										  double[] alfAssociationRate,
										  double[] alfConsultationTime,
										  double[] alfOperationTime,
										  double[] alfEmergencyTime,
										  int[] aiDepartment,
										  int[] aiRoomNumber )
	{
		int i;

		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
		{
			ArrayListDoctorAgents.get(i).vSetYearExperience( alfYearExperience[i] );
			ArrayListDoctorAgents.get(i).vSetConExperience( alfConExperience[i] );
			ArrayListDoctorAgents.get(i).vSetConTired1( alfConTired1[i] );
			ArrayListDoctorAgents.get(i).vSetConTired2( alfConTired2[i] );
			ArrayListDoctorAgents.get(i).vSetConTired3( alfConTired3[i] );
			ArrayListDoctorAgents.get(i).vSetConTired4( alfConTired4[i] );
			ArrayListDoctorAgents.get(i).vSetTiredRate( alfTiredRate[i] );
			ArrayListDoctorAgents.get(i).vSetRevisedOperationRate( alfRevisedOperationRate[i] );
			ArrayListDoctorAgents.get(i).vSetAssociationRate( alfAssociationRate[i] );
			ArrayListDoctorAgents.get(i).vSetConsultationTime( alfConsultationTime[i] );
			ArrayListDoctorAgents.get(i).vSetOperationTime( alfOperationTime[i] );
			ArrayListDoctorAgents.get(i).vSetEmergencyTime( alfEmergencyTime[i] );
			ArrayListDoctorAgents.get(i).vSetDoctorDepartment( aiDepartment[i] );
			ArrayListDoctorAgents.get(i).vSetExperienceRate1( alfExperienceRate1[i] );
			ArrayListDoctorAgents.get(i).vSetExperienceRate2( alfExperienceRate2[i] );
			ArrayListDoctorAgents.get(i).vSetConExperienceAIS( alfConExperienceAIS[i] );
			ArrayListDoctorAgents.get(i).vSetExperienceRateAIS1( alfExperienceRateAIS1[i] );
			ArrayListDoctorAgents.get(i).vSetExperienceRateAIS2( alfExperienceRateAIS2[i] );
			ArrayListDoctorAgents.get(i).vSetRoomNumber( aiRoomNumber[i] );
		}
	}


	/**
	 * <PRE>
	 *    HCUの看護師エージェントのパラメータを設定します。
	 * </PRE>
	 * @param aiNurseCategory				看護師のカテゴリー
	 * @param aiNurseTriageProtocol			トリアージプロトコル
	 * @param aiNurseTriageLevel			トリアージの緊急度レベル
	 * @param alfNurseTriageYearExperience	トリアージ経験年数
	 * @param alfNurseYearExperience		看護師経験年数
	 * @param alfNurseConExperience			看護師経験年数重み
	 * @param alfExperienceRate1			経験年数パラメータその１
	 * @param alfExperienceRate2			経験年数パラメータその２
	 * @param alfConExperienceAIS			経験年数パラメータ重み（重症度）
	 * @param alfExperienceRateAIS1			経験年数パラメータその１（重症度）
	 * @param alfExperienceRateAIS2			経験年数パラメータその２（重症度）
	 * @param alfNurseConTired1				疲労パラメータ１
	 * @param alfNurseConTired2				疲労パラメータ２
	 * @param alfNurseConTired3				疲労パラメータ３
	 * @param alfNurseConTired4				疲労パラメータ４
	 * @param alfNurseTiredRate				疲労度の割合
	 * @param alfNurseAssociationRate		連携度
	 * @param alfObservationTime			定期観察時間
	 * @param alfObservationProcessTime		観察プロセス時間
	 * @param alfTriageTime					トリアージ時間
	 * @param aiDepartment					所属部門
	 * @param aiRoomNumber					所属部屋番号
	 * @author kobayashi
	 * @since 2015/08/10
	 * version 0.2
	 */
	public void vSetNurseAgentParameter( int[] aiNurseCategory,
			int[] aiNurseTriageProtocol,
			int[] aiNurseTriageLevel,
			double[] alfNurseTriageYearExperience,
			double[] alfNurseYearExperience,
			double[] alfNurseConExperience,
			double[] alfExperienceRate1,
			double[] alfExperienceRate2,
			double[] alfConExperienceAIS,
			double[] alfExperienceRateAIS1,
			double[] alfExperienceRateAIS2,
			double[] alfNurseConTired1,
			double[] alfNurseConTired2,
			double[] alfNurseConTired3,
			double[] alfNurseConTired4,
			double[] alfNurseTiredRate,
			double[] alfNurseAssociationRate,
			double[] alfObservationTime,
			double[] alfObservationProcessTime,
			double[] alfTriageTime,
			int[] aiDepartment,
			int[] aiRoomNumber )
	{
		int i;

		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetNurseCategory( aiNurseCategory[i] );
			ArrayListNurseAgents.get(i).vSetTriageProtocol( aiNurseTriageProtocol[i] );
			ArrayListNurseAgents.get(i).vSetTriageProtocolLevel( aiNurseTriageLevel[i] );
			ArrayListNurseAgents.get(i).vSetTriageYearExperience( alfNurseTriageYearExperience[i] );
			ArrayListNurseAgents.get(i).vSetYearExperience( alfNurseYearExperience[i] );
			ArrayListNurseAgents.get(i).vSetConExperience( alfNurseConExperience[i] );
			ArrayListNurseAgents.get(i).vSetConTired1( alfNurseConTired1[i] );
			ArrayListNurseAgents.get(i).vSetConTired2( alfNurseConTired2[i] );
			ArrayListNurseAgents.get(i).vSetConTired3( alfNurseConTired3[i] );
			ArrayListNurseAgents.get(i).vSetConTired4( alfNurseConTired4[i] );
			ArrayListNurseAgents.get(i).vSetTiredRate( alfNurseTiredRate[i] );
			ArrayListNurseAgents.get(i).vSetAssociationRate( alfNurseAssociationRate[i] );
			ArrayListNurseAgents.get(i).vSetObservationTime( alfObservationTime[i] );
			ArrayListNurseAgents.get(i).vSetObservationProcessTime( alfObservationProcessTime[i] );
			ArrayListNurseAgents.get(i).vSetTriageProtocolTime( alfTriageTime[i] );
			ArrayListNurseAgents.get(i).vSetNurseDepartment( aiDepartment[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRate1( alfExperienceRate1[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRate2( alfExperienceRate2[i] );
			ArrayListNurseAgents.get(i).vSetConExperienceAIS( alfConExperienceAIS[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRateAIS1( alfExperienceRateAIS1[i] );
			ArrayListNurseAgents.get(i).vSetExperienceRateAIS2( alfExperienceRateAIS2[i] );
			ArrayListNurseAgents.get(i).vSetRoomNumber( aiRoomNumber[i] );
		}
	}

	/**
	 * <PRE>
	 *    FUSEエンジンにエージェントを登録します。
	 * </PRE>
	 * @param engine	FUSEのシミュレーションエンジン
	 */
	public void vSetSimulationEngine( SimulationEngine engine )
	{
		engine.addAgent(this);
	}

	/**
	 * <PRE>
	 *    観察室の観察プロセスを実行します。
	 * </PRE>
	 * @param ArrayListOperationRooms					全手術室
	 * @param ArrayListIntensiveCareUnitRooms			全集中治療室
	 * @param ArrayListGeneralWardRooms					全一般病棟
	 * @throws ERDoctorAgentException					医師エージェント例外クラス
	 */
	public void vImplementHighCareUnitRoom(  ArrayList<EROperationRoom> ArrayListOperationRooms, ArrayList<ERIntensiveCareUnitRoom> ArrayListIntensiveCareUnitRooms, ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms ) throws ERDoctorAgentException
	{
		int i,j;
		int iEnableNurse = -1;
		// 登録されている患者がいる場合は各看護師の観察を実行します。
		// いない場合は何もせずに終了します。
		synchronized( csHighCareUnitCriticalSection )
		{
			if( ArrayListPatientAgents.isEmpty() == false )
			{
				// 各看護師の観察を実行します。
				for( i = 0 ;i < ArrayListNurseAgents.size(); i++ )
				{
					if( ArrayListNursePatientLoc.get(i) >= 0 )
					{
						// マルチスレッドによる齟齬への対処
						if( ArrayListPatientAgents.size() <= 0 ) 										continue;
						if( ArrayListPatientAgents.size() <= ArrayListNursePatientLoc.get(i) ) 			continue;
						if( ArrayListPatientAgents.get( ArrayListNursePatientLoc.get( i ) ) == null )	continue;

						vImplementHighCareUnitRoomProcess( ArrayListOperationRooms, ArrayListIntensiveCareUnitRooms, ArrayListGeneralWardRooms, erHighCareUnitDoctorAgent, ArrayListNurseAgents.get( i ), ArrayListPatientAgents.get( ArrayListNursePatientLoc.get( i ) ), i );
					}
				}
			}
		}
	}

	/**
	 * <PRE>
	 *     高度治療室で処置の段階に入ったかどうかを判定します。
	 *     また、優先フラグを持っている患者エージェントは
	 *     優先フラグが有効になっている部屋に優先的に移動できるようにします。
	 * </PRE>
	 * @param ArrayListOperationRooms			全手術室
	 * @param ArrayListIntensiveCareUnitRooms	全集中治療室
	 * @param ArrayListGeneralWardRooms			全一般病棟
	 * @param erDoctorAgent						担当する医師エージェント
	 * @param erNurseAgent						担当する看護師エージェント
	 * @param erPAgent							処置を受ける患者エージェント
	 * @param iLoc								看護師と患者の対応する番号
	 * @throws ERDoctorAgentException			医師エージェントの例外クラス
	 */
	private void vImplementHighCareUnitRoomProcess( ArrayList<EROperationRoom> ArrayListOperationRooms, ArrayList<ERIntensiveCareUnitRoom> ArrayListIntensiveCareUnitRooms, ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms, ERDoctorAgent erDoctorAgent, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc ) throws ERDoctorAgentException
	{
		int i;
		// 医師エージェントの対応を実施します。
		erDoctorAgent.vSetAttending( 1 );

		// 看護師エージェントの対応を実施します。
		erNurseAgent.vSetAttending( 1 );

		if( erPAgent.isMoveWaitingTime() == false )
		{
			erDoctorAgent.vSetPatientMoveWaitFlag( 1 );
			erNurseAgent.vSetPatientMoveWaitFlag( 1 );
			erPAgent.vSetMoveWaitFlag( 1 );
			// 移動時間がまだ終了していないので、移動を実施しません。
			cHighCareUnitLog.info(erPAgent.getId() + "," + "高度治療室移動時間：" + erPAgent.lfGetMoveWaitingTime() );
			return;
		}
		// 部屋移動が終了したのでフラグOFFに処置中とします。
		erPAgent.vSetMoveRoomFlag( 0 );
		erDoctorAgent.vSetPatientMoveWaitFlag( 0 );
		erNurseAgent.vSetPatientMoveWaitFlag( 0 );
		erPAgent.vSetMoveWaitFlag( 0 );

		// その患者を対応している医師、看護師エージェントのIDを設定します。
		erPAgent.vSetNurseAgent(erNurseAgent.getId());
		erPAgent.vSetDoctorAgent(erDoctorAgent.getId());

		cHighCareUnitLog.info(erPAgent.getId() + "," + "高度治療室入院中" );

		// 初療室待機フラグがONの場合
		if( erPAgent.iGetOperationRoomWaitFlag() == 1 )
		{
			// 初療室へ移動します。
			vJudgeMoveOperationRoom( ArrayListOperationRooms, erDoctorAgent, erNurseAgent, erPAgent, iLoc );
			// 移動できるので、移動します。
			if( erPAgent.iGetOperationRoomWaitFlag() == 0 ) return ;
		}
		// 重症観察室フラグがONの場合
		if( erPAgent.iGetIntensiveCareUnitRoomWaitFlag() == 1 )
		{
			// 集中治療室へ移動します。
			vJudgeMoveIntensiveCareUnitRoom( ArrayListIntensiveCareUnitRooms, ArrayListGeneralWardRooms, erDoctorAgent, erNurseAgent, erPAgent, iLoc );
			// 移動できるので、移動します。
			if( erPAgent.iGetHighCareUnitRoomWaitFlag() == 0 ) return ;
		}

		// 患者に一般病棟フラグがONの場合
		if( erPAgent.iGetGeneralWardRoomWaitFlag() == 1 )
		{
			// 一般病棟へ移動できる場合は移動します。
			vJudgeMoveGeneralWardRoom( ArrayListGeneralWardRooms, erDoctorAgent, erNurseAgent, erPAgent, iLoc );
			// 移動できるので、移動します。
			if( erPAgent.iGetGeneralWardRoomWaitFlag() == 0 ) return ;
		}
		vImplementHighCareUnitProcess( ArrayListOperationRooms, ArrayListIntensiveCareUnitRooms, ArrayListGeneralWardRooms, erDoctorAgent, erNurseAgent, erPAgent, iLoc );
	}

	/**
	 * <PRE>
	 *     高度治療室での処置を行います。
	 * </PRE>
	 * @param ArrayListOperationRooms			全手術室
	 * @param ArrayListIntensiveCareUnitRooms	全集中治療室
	 * @param ArrayListGeneralWardRooms			全一般病棟
	 * @param erDoctorAgent						担当する医師エージェント
	 * @param erNurseAgent						担当する看護師エージェント
	 * @param erPAgent							処置を受ける患者エージェント
	 * @param iLoc								看護師と患者の対応する番号
	 * @throws ERDoctorAgentException			医師エージェントの例外クラス
	 */
	private void vImplementHighCareUnitProcess( ArrayList<EROperationRoom> ArrayListOperationRooms, ArrayList<ERIntensiveCareUnitRoom> ArrayListIntensiveCareUnitRooms, ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms, ERDoctorAgent erDoctorAgent, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc ) throws ERDoctorAgentException
	{
		int i;
		double lfMaxAIS = 0.0;
		double lfAISRevisedSeries = 0.0;
		double lfHospitalStayDay = 0.0;
		double lfInitAIS = 0.0;
		cHighCareUnitLog.fine(erPAgent.getId() + "," + erDoctorAgent.getId() +"," + "患者の入院時間：" + erPAgent.lfGetHospitalStayTime() + "," + "患者の待ち時間：" + erPAgent.lfGetMoveWaitingTime() + "," + "患者傷病状態改善度：" + erPAgent.lfGetRevisedSeries());
		if( erPAgent.lfGetHospitalStayTime()-erPAgent.lfGetMoveWaitingTime() <= 0.0 && erPAgent.lfGetRevisedSeries() == 0.0 )
		{
			// 入院時のAIS値を設定します。
			lfInitAIS = erPAgent.lfSetHospitalStayInitAIS();

			// 入院日数を算出します。
			lfHospitalStayDay = lfCalcHospitalStay( erPAgent );

			// 患者の入院日数を設定します。
			erPAgent.vSetHospitalStayDay( lfHospitalStayDay );

			// AIS値改善割合を算出します。
			double lfStepTime = this.getEngine().getLatestTimeStep()/1000.0;
			double lfStayTime = lfStepTime / (24.0*3600.0);
			lfAISRevisedSeries = lfCurePatientAISSeries( lfStayTime, lfHospitalStayDay, 0.5, lfInitAIS );

			// AIS値改善割合を設定します。
			erPAgent.vSetRevisedSeries( lfAISRevisedSeries );
		}

		// 初めて高度治療室に入った場合に算出します。
		if( erPAgent.iGetEnterHighCareUnitFlag() == 0 )
		{
			cHighCareUnitLog.info(erPAgent.getId() + "," + erDoctorAgent.getId() +"," + "高度治療室の平均在院日数を算出します。");
			// 平均在院日数を算出します。
			vCalcHcuAvgLengthOfStay();
			erPAgent.vSetEnterHighCareUnitFlag( 1 );
		}

		// 患者のAIS値を改善させます。（時間経過とともに）
		vCurePatientAIS( erPAgent );

		// 医師の判定を更新します。
		erDoctorAgent.iJudgeAIS( erPAgent );

		// ここにHCUに来た場合、各病院の平均在院日数はいることとします。
		if( erPAgent.lfGetHighCareUnitStayTime() < lfHcuAvgLengthOfStay )
		{
			// 平均4.53625日
			// 在院日数が経過していないので、ここで終了します。
			cHighCareUnitLog.info(erPAgent.getId() + "," + erDoctorAgent.getId() +"," + "現在の在院日数：" + erPAgent.lfGetHospitalStayTime() );
			return ;
		}

		// 医師エージェントが患者エージェントが集中治療室へ移動かどうかを判定します。
		if( erDoctorAgent.isJudgeIntensiveCareUnit( erPAgent ) == true )
		{
			// 集中治療室への移動判定を実行します。
			vJudgeMoveIntensiveCareUnitRoom( ArrayListIntensiveCareUnitRooms, ArrayListGeneralWardRooms, erDoctorAgent, erNurseAgent, erPAgent, iLoc );
		}
		else
		{
			// 手術が必要かどうかの判定を実施します。
			if( erDoctorAgent.isJudgeOperation( erPAgent ) == true )
			{
				// 手術室移動待機判定フラグをONにします。
				erPAgent.vSetOperationRoomWaitFlag( 1 );

				// 手術室移動判定を実施します。
				vJudgeMoveOperationRoom( ArrayListOperationRooms, erDoctorAgent, erNurseAgent, erPAgent, iLoc );
			}
			else
			{
				// 医師エージェントが患者エージェントが一般病棟へ移動可能かどうかを判定します。
				if( erDoctorAgent.isJudgeGeneralWard( erPAgent ) == true )
				{
					// 一般病棟への移動判定を実行します。
					vJudgeMoveGeneralWardRoom( ArrayListGeneralWardRooms, erDoctorAgent, erNurseAgent, erPAgent, iLoc );
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    AIS値の改善を行います。
	 *    簡易的にここでは、指数関数を掛け合わせることで、AIS値を時間とともに低下させます。
	 * </PRE>
	 * @param erPatientAgent	処置を受けている患者エージェント
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vCurePatientAIS( ERPatientAgent erPatientAgent )
	{
		double lfStepTime = 0.0;
		double lfStayTime = 0.0;
		double lfCurrentAISHead = erPatientAgent.lfGetInternalAISHead();
		double lfCurrentAISFace = erPatientAgent.lfGetInternalAISFace();
		double lfCurrentAISNeck = erPatientAgent.lfGetInternalAISNeck();
		double lfCurrentAISThorax = erPatientAgent.lfGetInternalAISThorax();
		double lfCurrentAISAbdomen = erPatientAgent.lfGetInternalAISAbdomen();
		double lfCurrentAISSpine = erPatientAgent.lfGetInternalAISSpine();
		double lfCurrentAISUpperExtremity = erPatientAgent.lfGetInternalAISUpperExtremity();
		double lfCurrentAISLowerExtremity = erPatientAgent.lfGetInternalAISLowerExtremity();
		double lfCurrentAISUnspecified = erPatientAgent.lfGetInternalAISUnspecified();

		double lfHospitalStayDay = 0.0;
		double lfRevisedSeries = 0.0;

		lfHospitalStayDay = erPatientAgent.lfGetHospitalStayDay();
		lfRevisedSeries = erPatientAgent.lfGetRevisedSeries();

		// AIS値を改善させます。
		lfStepTime = this.getEngine().getLatestTimeStep()/1000.0;
		lfStayTime = lfStepTime / (24.0*3600.0);
		lfCurrentAISHead = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISHead );
		lfCurrentAISFace = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISFace );
		lfCurrentAISNeck = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISNeck );
		lfCurrentAISThorax = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISThorax );
		lfCurrentAISAbdomen = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISAbdomen );
		lfCurrentAISSpine = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISSpine );
		lfCurrentAISUpperExtremity = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISUpperExtremity );
		lfCurrentAISLowerExtremity = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISLowerExtremity );
		lfCurrentAISUnspecified = lfCurePatientAISFunction( lfStayTime, lfHospitalStayDay, lfRevisedSeries, lfCurrentAISUnspecified );

		// 改善した内容を代入します。
		erPatientAgent.vSetInternalAISHead( lfCurrentAISHead );
		erPatientAgent.vSetInternalAISFace( lfCurrentAISFace );
		erPatientAgent.vSetInternalAISNeck( lfCurrentAISNeck );
		erPatientAgent.vSetInternalAISThorax( lfCurrentAISThorax );
		erPatientAgent.vSetInternalAISAbdomen( lfCurrentAISAbdomen );
		erPatientAgent.vSetInternalAISSpine( lfCurrentAISSpine );
		erPatientAgent.vSetInternalAISUpperExtremity( lfCurrentAISUpperExtremity );
		erPatientAgent.vSetInternalAISLowerExtremity( lfCurrentAISLowerExtremity );
		erPatientAgent.vSetInternalAISUnspecified( lfCurrentAISUnspecified );

		cHighCareUnitLog.info(erPatientAgent.getId() + "," + erPatientAgent.lfGetHospitalStayTime() + "," + "各部位の傷病状態"+ "," + erPatientAgent.lfGetInternalAISHead() + "," + erPatientAgent.lfGetInternalAISFace() + "," + erPatientAgent.lfGetInternalAISNeck() + "," +
		erPatientAgent.lfGetInternalAISThorax() + "," + erPatientAgent.lfGetInternalAISAbdomen() + "," + erPatientAgent.lfGetInternalAISSpine() + "," +
		erPatientAgent.lfGetInternalAISUpperExtremity() + "," + erPatientAgent.lfGetInternalAISLowerExtremity() + "," + erPatientAgent.lfGetInternalAISUnspecified());

		erPatientAgent.vStrSetInjuryStatus();
	}

	/**
	 * <PRE>
	 *    患者の改善をします。
	 *    厚生労働省の統計データから退院日数を基に等比級数の級数を設定するようにします。
	 *    AIS値は0.5以下になった場合を退院とするので、そこまで値が低下することを前提とします。
	 * </PRE>
	 * @param lfTime				現在の経過時間[秒]を設定します。
	 * @param lfHospitalStayTime	病院に入院する日数を設定します。[日]
	 * @param lfFinishAIS			最終的なAIS値を設定します。
	 * @param lfInitAIS				初期のAIS値を設定します。
	 * @return	改善後のAIS値
	 */
	private double lfCurePatientAISSeries( double lfTime, double lfHospitalStayTime, double lfFinishAIS, double lfInitAIS )
	{
		double lfSeries = 0.0;
		double lfX = 0.0;
		double lfN = 0.0;
		double lfDeltaTime = 0.0;

		double lfStepTime = this.getEngine().getLatestTimeStep()/1000.0;

		// シミュレーション時間間隔を考慮して級数を算出します。
		lfDeltaTime = lfStepTime/86400.0;
		lfN = lfHospitalStayTime*86400;
		lfSeries = Math.pow( lfFinishAIS/lfInitAIS, 1.0/((lfN-1)*lfDeltaTime) );
		return lfSeries;
	}

	/**
	 * <PRE>
	 *    患者の改善をします。
	 *    厚生労働省の統計データから退院日数を基に等比級数の級数を設定するようにします。
	 *    AIS値は0.5以下になった場合を退院とするので、そこまで値が低下することを前提とします。
	 * </PRE>
	 * @param lfTime				現在の経過時間[秒]を設定します。
	 * @param lfHospitalStayTime	病院に入院する日数を設定します。[日]
	 * @param lfRevisedSeriesAIS	改善するためのベースとなるAIS値を設定します。
	 * @param lfCurrentAIS			現在のAIS値を設定します。
	 * @return	改善後のAIS値
	 */
	private double lfCurePatientAISFunction( double lfTime, double lfHospitalStayTime, double lfRevisedSeriesAIS, double lfCurrentAIS )
	{
		double lfRes = 0.0;

		lfRes = lfCurrentAIS*Math.pow( lfRevisedSeriesAIS, lfTime );
		return lfRes;
	}

	/**
	 * <PRE>
	 *    入院日数を算出します。単位[日]
	 *    厚生労働省の統計データより
	 * </PRE>
	 * @param erPatientAgent 対象とする患者エージェント
	 * @return	入院日数
	 */
	private double lfCalcHospitalStay( ERPatientAgent erPatientAgent )
	{
		double lfHospitalStayTime = 0.0;
		double lfMaxHospitalStayTime = -Double.MAX_VALUE;

		if( 0 < erPatientAgent.lfGetInternalAISHead() && erPatientAgent.lfGetInternalAISHead() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 16.64048 + 2*(rnd.NextUnif()-1)*11.08299;
			else								lfHospitalStayTime = 17.40952 + 2*(rnd.NextUnif()-1)*15.05288;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 16.64048 + 2*(rnd.NextUnif()-1)*11.08299;
			else								lfHospitalStayTime = 17.40952 + 2*(rnd.NextUnif()-1)*15.05288;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISFace() && erPatientAgent.lfGetInternalAISFace() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 9.695238 + 2*(rnd.NextUnif()-1)*6.306905;
			else								lfHospitalStayTime = 8.173684 + 2*(rnd.NextUnif()-1)*4.925591;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 9.695238 + 2*(rnd.NextUnif()-1)*6.306905;
			else								lfHospitalStayTime = 8.173684 + 2*(rnd.NextUnif()-1)*4.925591;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISNeck() && erPatientAgent.lfGetInternalAISNeck() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 29.41905 + 2*(rnd.NextUnif()-1)*16.41072;
			else								lfHospitalStayTime = 39.86000 + 2*(rnd.NextUnif()-1)*33.62594;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 29.41905 + 2*(rnd.NextUnif()-1)*16.41072;
			else								lfHospitalStayTime = 39.86000 + 2*(rnd.NextUnif()-1)*33.62594;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISThorax() && erPatientAgent.lfGetInternalAISThorax() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 22.65714 + 2*(rnd.NextUnif()-1)*14.36938;
			else								lfHospitalStayTime = 26.66410 + 2*(rnd.NextUnif()-1)*28.15095;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 22.65714 + 2*(rnd.NextUnif()-1)*14.36938;
			else								lfHospitalStayTime = 26.66410 + 2*(rnd.NextUnif()-1)*28.15095;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISAbdomen() && erPatientAgent.lfGetInternalAISAbdomen() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 16.45122 + 2*(rnd.NextUnif()-1)*13.50604;
			else								lfHospitalStayTime = 17.89524 + 2*(rnd.NextUnif()-1)*20.43413;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 16.45122 + 2*(rnd.NextUnif()-1)*13.50604;
			else								lfHospitalStayTime = 17.89524 + 2*(rnd.NextUnif()-1)*20.43413;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISSpine() && erPatientAgent.lfGetInternalAISSpine() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 29.41905 + 2*(rnd.NextUnif()-1)*16.41072;
			else								lfHospitalStayTime = 39.86000 + 2*(rnd.NextUnif()-1)*33.62594;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 29.41905 + 2*(rnd.NextUnif()-1)*16.41072;
			else								lfHospitalStayTime = 39.86000 + 2*(rnd.NextUnif()-1)*33.62594;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISLowerExtremity() && erPatientAgent.lfGetInternalAISLowerExtremity() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 18.26308 + 2*(rnd.NextUnif()-1)*10.95915;
			else								lfHospitalStayTime = 17.73750 + 2*(rnd.NextUnif()-1)*12.94469;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 18.26308 + 2*(rnd.NextUnif()-1)*10.95915;
			else								lfHospitalStayTime = 17.73750 + 2*(rnd.NextUnif()-1)*12.94469;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISUpperExtremity() && erPatientAgent.lfGetInternalAISUpperExtremity() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 27.73182 + 2*(rnd.NextUnif()-1)*17.39286;
			else								lfHospitalStayTime = 17.73750 + 2*(rnd.NextUnif()-1)*12.94469;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 27.73182 + 2*(rnd.NextUnif()-1)*17.39286;
			else								lfHospitalStayTime = 17.73750 + 2*(rnd.NextUnif()-1)*12.94469;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		if( 0 < erPatientAgent.lfGetInternalAISUnspecified() && erPatientAgent.lfGetInternalAISUnspecified() < 3 )
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 32.96429 + 2*(rnd.NextUnif()-1)*30.50811;
			else								lfHospitalStayTime = 31.02381 + 2*(rnd.NextUnif()-1)*20.56167;
		}
		else
		{
			if( erPatientAgent.iGetSex() == 1 ) lfHospitalStayTime = 32.96429 + 2*(rnd.NextUnif()-1)*30.50811;
			else								lfHospitalStayTime = 31.02381 + 2*(rnd.NextUnif()-1)*20.56167;
		}
		lfMaxHospitalStayTime = lfHospitalStayTime > lfMaxHospitalStayTime ? lfHospitalStayTime : lfMaxHospitalStayTime;
		cHighCareUnitLog.info(erPatientAgent.getId() + "," +  "高度治療室に在院する時間," + lfMaxHospitalStayTime );
		return lfMaxHospitalStayTime;
	}

	/**
	 * <PRE>
	 *    手術室へ移動可能かどうかを判定し、移動できる場合は待合室へ移動します。
	 *    移動できない場合は待合室へいったん移動します。
	 * </PRE>
	 * @param ArrayListOperationRooms	全手術室
	 * @param erDoctorAgent				担当した医師エージェント
	 * @param erNurseAgent				担当した看護師エージェント
	 * @param erPAgent					移動する患者エージェント
	 * @param iLoc						看護師に対応する患者エージェント番号
	 * @author kobayashi
	 * @since 2015/08/04
	 */
	private void vJudgeMoveOperationRoom( ArrayList<EROperationRoom> ArrayListOperationRooms, ERDoctorAgent erDoctorAgent, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc )
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erOperationDoctorAgent;
		ERNurseAgent erOperationNurseAgent;
		for( i = 0;i < ArrayListOperationRooms.size(); i++ )
		{
			// 手術室に空きがある場合
			if( ArrayListOperationRooms.get(i).isVacant() == true )
			{
				// 手術室待ちフラグをOFFにします。
				erPAgent.vSetOperationRoomWaitFlag( 0 );

				// 患者のいる位置を手術室に変更します。
				erPAgent.vSetLocation( 2 );

				// 観察フラグをOFFにします。
				erPAgent.vSetObservedFlag( 0 );

				// 観察室での看護師に見てもらったフラグはOFFにします。
				erPAgent.vSetNurseAttended( 0 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 高度治療室から移動するので0とします。
				erPAgent.vSetEnterHighCareUnitFlag( 0 );

				// 診察室へ患者エージェントを移動します。
				ArrayListOperationRooms.get(i).vSetPatientAgent( erPAgent );

			// 医師、看護師エージェントへメッセージを送信します。
				// 手術室の医師エージェントへメッセージを送信します。
				erOperationDoctorAgent = ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent();
				erDoctorAgent.vSendToDoctorAgentMessage( erPAgent, (int)erDoctorAgent.getId(), (int)erOperationDoctorAgent.getId() );

				// 手術室の看護師エージェントへメッセージを送信します。
				for( j = 0;j < ArrayListOperationRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erOperationNurseAgent = ArrayListOperationRooms.get(i).cGetNurseAgent(j);
					erDoctorAgent.vSendToNurseAgentMessage( erPAgent, erOperationNurseAgent, (int)erDoctorAgent.getId(), (int)erOperationNurseAgent.getId() );
				}

				// 医師エージェントの対応を終了します。
				erDoctorAgent.vSetAttending( 0 );

				// 看護師エージェントの対応を終了します。
				erNurseAgent.vSetAttending( 0 );

				// 対応を受けた患者エージェントを削除します。
				vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
				ArrayListNursePatientLoc.set( iLoc, -1 );

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListOperationRooms.get(i).erGetTriageNode() ) );
				}
				cHighCareUnitLog.info(erPAgent.getId() + "," +erPAgent.iGetLocation() + "," +  "手術室移動準備完了" );
				// 手術室で担当する医師エージェントを設定します。
				ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent().vSetSurgeon(1);
				ArrayListOperationRooms.get(i).cGetSurgeonDoctorAgent().vSetAttending(1);

				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListOperationRooms.size() )
		{
			// 空きがない場合は患者の手術室待ちフラグをONにします。
			erPAgent.vSetOperationRoomWaitFlag( 1 );

			// そのまま待機します。
		}
	}

	/**
	 * <PRE>
	 *   一般病棟への移動判定を行います。
	 * </PRE>
	 * @param ArrayListGeneralWardRooms	全一般病棟
	 * @param erDoctorAgent				担当した医師エージェント
	 * @param erNurseAgent				担当した看護師エージェント
	 * @param erPAgent					移動する患者エージェント
	 * @param iLoc						看護師に対応する患者エージェント番号
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	private void vJudgeMoveGeneralWardRoom( ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms, ERDoctorAgent erDoctorAgent, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc )
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erGeneralWardDoctorAgent;
		ERNurseAgent erGeneralWardNurseAgent;
		for( i = 0;i < ArrayListGeneralWardRooms.size(); i++ )
		{
			// 一般病棟に空きがある場合
			if( ArrayListGeneralWardRooms.get(i).isVacant() == true )
			{
				// 一般病棟待機フラグをOFFにします。
				erPAgent.vSetGeneralWardRoomWaitFlag( 0 );

				// 患者のいる位置を一般病棟に変更します。
				erPAgent.vSetLocation( 8 );

				// 観察フラグをOFFにします。
				erPAgent.vSetObservedFlag( 0 );

				// 観察室での看護師に見てもらったフラグはOFFにします。
				erPAgent.vSetNurseAttended( 0 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 高度治療室から移動するので0とします。
				erPAgent.vSetEnterHighCareUnitFlag( 0 );

				// 初療室へ患者エージェントを移動します。
				ArrayListGeneralWardRooms.get(i).vSetPatientAgent( erPAgent );

			// 看護師、医師エージェントへメッセージを送信します。
				// 医師へメッセージを送信します。
				for(j = 0;j < ArrayListGeneralWardRooms.get(i).iGetDoctorAgentsNum(); j++ )
				{
					// 看護師、医師、技士エージェントへメッセージを送信します。
					erGeneralWardDoctorAgent = ArrayListGeneralWardRooms.get(i).cGetDoctorAgent(j);
					erDoctorAgent.vSendToDoctorAgentMessage( erPAgent, (int)erDoctorAgent.getId(), (int)erGeneralWardDoctorAgent.getId() );
				}
				for(j = 0;j < ArrayListGeneralWardRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erGeneralWardNurseAgent = ArrayListGeneralWardRooms.get(i).cGetNurseAgent(j);
					// 看護師、医師、技士エージェントへメッセージを送信します。
					erDoctorAgent.vSendToNurseAgentMessage( erPAgent, erGeneralWardNurseAgent, (int)erDoctorAgent.getId(), (int)erGeneralWardNurseAgent.getId() );
				}

				// 医師エージェントの対応を終了します。
				erDoctorAgent.vSetAttending( 0 );

				// 看護師エージェントの対応を終了します。
				erNurseAgent.vSetAttending( 0 );

				// 対応を受けた患者エージェントを削除します。
				vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
				ArrayListNursePatientLoc.set( iLoc, -1 );

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListGeneralWardRooms.get(i).erGetTriageNode() ) );
				}
				cHighCareUnitLog.info(erPAgent.getId() + "," +erPAgent.iGetLocation() + "," +  "一般病棟移動準備完了" );

				erPAgent = null;

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListGeneralWardRooms.size() )
		{
			// 空きがない場合は一般病棟待機フラグをONにしてそのまま一般病棟の空きを判定します。
			erPAgent.vSetGeneralWardRoomWaitFlag( 1 );
		}
	}

	/**
	 * <PRE>
	 *    集中治療室への移動判定を行います。
	 * </PRE>
	 * @param ArrayListIntensiveCareUnitRooms	全集中治療室
	 * @param ArrayListGeneralWardRooms			全一般病棟
	 * @param erDoctorAgent						担当した医師エージェント
	 * @param erNurseAgent						担当した看護師エージェント
	 * @param erPAgent							処置を受けている患者エージェント
	 * @param iLoc								看護師に対応する患者エージェント番号
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	private void vJudgeMoveIntensiveCareUnitRoom( ArrayList<ERIntensiveCareUnitRoom> ArrayListIntensiveCareUnitRooms, ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms, ERDoctorAgent erDoctorAgent, ERNurseAgent erNurseAgent, ERPatientAgent erPAgent, int iLoc )
	{
		int i,j;
		int iJudgeCount = 0;
		ERDoctorAgent erEmergencyDoctorAgent;
		ERNurseAgent erEmergencyNurseAgent;
		for( i = 0;i < ArrayListIntensiveCareUnitRooms.size(); i++ )
		{
			// 集中治療室に空きがある場合は観察室へエージェントを移動します。
			if( ArrayListIntensiveCareUnitRooms.get(i).isVacant() == true )
			{
				// 集中治療室待機フラグをOFFにします。
				erPAgent.vSetIntensiveCareUnitRoomWaitFlag( 0 );

				// 患者のいる位置を集中治療室に変更します。
				erPAgent.vSetLocation( 6 );

				// 観察フラグをOFFにします。
				erPAgent.vSetObservedFlag( 0 );

				// 観察室での看護師に見てもらったフラグはOFFにします。
				erPAgent.vSetNurseAttended( 0 );

				// 移動開始フラグを設定します。
				erPAgent.vSetMoveRoomFlag( 1 );
				erPAgent.vSetMoveWaitingTime( 0.0 );

				// その患者を対応している医師、看護師エージェントのIDを0に設定します。
				erPAgent.vSetNurseAgent( 0 );
				erPAgent.vSetDoctorAgent( 0 );

				// 高度治療室から移動するので0とします。
				erPAgent.vSetEnterHighCareUnitFlag( 0 );

			// 空きがない場合は集中治療室へ移動します。
				ArrayListIntensiveCareUnitRooms.get(i).vSetPatientAgent( erPAgent );

				// 看護師エージェントへメッセージを送信します。
				for( j = 0;j < ArrayListIntensiveCareUnitRooms.get(i).iGetNurseAgentsNum(); j++ )
				{
					erEmergencyNurseAgent = ArrayListIntensiveCareUnitRooms.get(i).cGetNurseAgent(j);
					erDoctorAgent.vSendToNurseAgentMessage( erPAgent, erEmergencyNurseAgent, (int)erDoctorAgent.getId(), (int)erEmergencyNurseAgent.getId() );
				}
				// 医師エージェントへメッセージを送信します。
				for( j = 0;j < ArrayListIntensiveCareUnitRooms.get(i).iGetDoctorAgentsNum(); j++ )
				{
					erEmergencyDoctorAgent = ArrayListIntensiveCareUnitRooms.get(i).cGetDoctorAgent(j);
					erDoctorAgent.vSendToDoctorAgentMessage( erPAgent, (int)erDoctorAgent.getId(), (int)erEmergencyDoctorAgent.getId() );
				}

				// 医師エージェントの対応を終了します。
				erDoctorAgent.vSetAttending( 0 );

				// 看護師エージェントの対応を終了します。
				erNurseAgent.vSetAttending( 0 );

				// 対応を受けた患者エージェントを削除します。
				vRemovePatientAgent( erPAgent, ArrayListNursePatientLoc.get(iLoc) );
				ArrayListNursePatientLoc.set( iLoc, -1 );

				if( iInverseSimFlag == 1 )
				{
					// 移動先の経路を患者エージェントに設定します。
					erPAgent.vSetMoveRoute( erTriageNodeManager.getRoute( this.erGetTriageNode(), ArrayListIntensiveCareUnitRooms.get(i).erGetTriageNode() ) );
				}
				erPAgent = null;

				// 空いている看護師に割り当てます。
				ArrayListIntensiveCareUnitRooms.get(i).bAssignVacantNurse();

				break;
			}
			else
			{
				iJudgeCount++;
			}
		}
		if( iJudgeCount == ArrayListIntensiveCareUnitRooms.size() )
		{
			// 空きがない場合は高度治療室でそのまま待機とします。
		}
	}

	/**
	 * <PRE>
	 *    患者を一般病棟へ登録します。
	 * </PRE>
	 * @param erPAgent 患者エージェント
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vSetPatientAgent( ERPatientAgent erPAgent )
	{
		if( erPAgent.iGetStayHospitalFlag() == 0 )
		{
			// 患者が入院するフラグを有効にします。
			erPAgent.vSetStayHospitalFlag( 1 );
		}
		if( erPAgent.iGetStayHospitalTimeFlag() == 0 )
		{
			// 患者の入院経過時間の加算を開始します。
			erPAgent.vSetStayHospitalTimeFlag( 1 );
		}
		// 高度治療室フラグをONにし、他の入院病棟のフラグをOFFにします。
		erPAgent.vSetStayGeneralWardFlag( 0 );
		erPAgent.vSetStayHighCareUnitFlag( 1 );
		erPAgent.vSetStayIntensiveCareUnitFlag( 0 );

		// 高度治療室に患者を登録します。
		ArrayListPatientAgents.add( erPAgent );
	}

	/**
	 * <PRE>
	 *    対応が終了した患者を削除します。
	 * </PRE>
	 * @param erPatientAgent 患者エージェント
	 * @param iLocation 対応が終了した患者の番号
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public void vRemovePatientAgent( ERPatientAgent erPatientAgent, int iLocation )
	{
		int i;
		ArrayListPatientAgents.remove( erPatientAgent );
		for( i = 0;i < ArrayListNursePatientLoc.size(); i++ )
		{
			if( iLocation < ArrayListNursePatientLoc.get(i) )
			{
				ArrayListNursePatientLoc.set(i, ArrayListNursePatientLoc.get(i)-1 );
			}
		}
		erPatientAgent = null;
	}


	/**
	 * <PRE>
	 *   高度治療室の担当医師エージェントを取得します。
	 * </PRE>
	 * @return	担当する医師エージェント
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERDoctorAgent cGetHighCareUnitDoctorAgent()
	{
		return erHighCareUnitDoctorAgent;
	}

	/**
	 * <PRE>
	 *   高度治療室の医師エージェントを取得します。
	 * </PRE>
	 * @param i 所属している医師の番号
	 * @return	該当する医師エージェント
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERDoctorAgent cGetDoctorAgent( int i )
	{
		return ArrayListDoctorAgents.get(i);
	}

	/**
	 * <PRE>
	 *   高度治療室の看護師エージェントの数を取得します。
	 * </PRE>
	 * @return	所属する医師エージェントの人数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iGetDoctorAgentsNum()
	{
		return ArrayListDoctorAgents.size();
	}

	/**
	 * <PRE>
	 *   高度治療室の看護師エージェントを取得します。
	 * </PRE>
	 * @param i 所属している看護師の番号
	 * @return	該当する看護師エージェント
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public ERNurseAgent cGetNurseAgent( int i )
	{
		return ArrayListNurseAgents.get(i);
	}

	/**
	 * <PRE>
	 *   高度治療室の看護師エージェントの数を取得します。
	 * </PRE>
	 * @return	所属する看護師エージェントの人数
	 * @author kobayashi
	 * @since 2015/08/05
	 */
	public int iGetNurseAgentsNum()
	{
		return ArrayListNurseAgents.size();
	}

	/**
	 * <PRE>
	 *    高度治療室看護師が対応中かどうかを判定します。
	 * </PRE>
	 * @return false 全員対応している
	 *         true  空きの看護師がいる
	 * @author kobayashi
	 * @since 2015/08/03
	 */
	public boolean isVacant()
	{
		int i;
		int iCount = 0;
		boolean bRet = true;
		// 室としては存在しているが、看護師がいない場合は対応できないため、空いていないとします。
		// 通常はないはずだが・・・。
		if( ArrayListNurseAgents == null )
		{
			bRet = false;
		}
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			// 所属看護師が全員対応中の場合、空いていないとします。
			if( ArrayListNurseAgents.get(i).iGetAttending() == 1 )
			{
				iCount++;
			}
		}
		if( iCount == ArrayListNurseAgents.size() )
		{
			bRet = false;
		}
		return bRet;
	}

	/**
	 * <PRE>
	 *    高度治療室の平均在院日数を算出します。
	 * </PRE>
	 */
	public void vCalcHcuAvgLengthOfStay()
	{
		// 平均6.452222222日、標準偏差4.707047317日
		double lfHcuLengthOfStay = 0.0;
//		lfHcuLengthOfStay = 6.452222222 + rnd.NextUnif()*4.707047317;
		lfHcuLengthOfStay = 2.35;
		lfHcuAvgLengthOfStay = lfHcuLengthOfStay*86400;
	}

	/**
	 * <PRE>
	 *     移動する患者エージェントの変更を実施します。
	 * </PRE>
	 * @param erPAgent						対象とする患者エージェント
	 * @param ArrayListGeneralWardRooms		全一般病棟
	 * @param erDoctorAgent					担当する医師エージェント
	 * @return		true	移動する患者を変更して別の患者エージェントを一般病棟へ移動させます。
	 * 				false	対象を変更しません。
	 */
	public boolean bChangePatient( ERPatientAgent erPAgent, ArrayList<ERGeneralWardRoom> ArrayListGeneralWardRooms, ERDoctorAgent erDoctorAgent )
	{
		int i;
		int iLoc = 0;
		int iTargetCount = 0;
		int iTargetLoc = 0;
		int iMinEmergency = Integer.MAX_VALUE;
		double lfMinAIS = Double.MAX_VALUE;
		boolean bRet = false;

		// 移動する患者よりも緊急度が大きい患者がいないかどうかを確認します。
		for( i = 0;i < ArrayListPatientAgents.size(); i++ )
		{
			if( erPAgent.iGetEmergencyLevel() < ArrayListPatientAgents.get(i).iGetEmergencyLevel() )
			{
				iTargetCount++;
			}
		}
//		for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//		{
//			if( erPAgent.lfGetMaxAIS() > ArrayListPatientAgents.get(i).lfGetMaxAIS() )
//			{
//				iTargetCount++;
//			}
//		}
		if( iTargetCount > 0 )
		{
			// 緊急度により判定を実施します。
			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
			{
				if( iMinEmergency < ArrayListPatientAgents.get(i).iGetEmergencyLevel() )
				{
					iMinEmergency = ArrayListPatientAgents.get(i).iGetEmergencyLevel();
					iTargetLoc = i;
				}
			}
//			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//			{
//				if( lfMinAIS > ArrayListPatientAgents.get(i).lfGetMaxAIS() )
//				{
//					lfMinAIS = ArrayListPatientAgents.get(i).lfGetMaxAIS();
//					iTargetLoc = i;
//				}
//			}
			for( i = 0;i < ArrayListNursePatientLoc.size(); i++ )
			{
				if( ArrayListNursePatientLoc.get( i ) == iTargetLoc )
				{
					iLoc = i;
				}
			}
			// 一般病棟へ移動します。
			vJudgeMoveGeneralWardRoom( ArrayListGeneralWardRooms, erDoctorAgent, ArrayListNurseAgents.get(iLoc), ArrayListPatientAgents.get(iTargetLoc), iLoc );
			bRet = true;
		}
		else
		{
			iTargetCount = 0;
			// 緊急度に差がない場合は重症度により判定し、いないかどうかを判定します。
//			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//			{
//				if( erPAgent.iGetEmergencyLevel() < ArrayListPatientAgents.get(i).iGetEmergencyLevel() )
//				{
//					iTargetCount++;
//				}
//			}
			for( i = 0;i < ArrayListPatientAgents.size(); i++ )
			{
				if( erPAgent.lfGetMaxAIS() > ArrayListPatientAgents.get(i).lfGetMaxAIS() )
				{
					iTargetCount++;
				}
			}
			if( iTargetCount > 0 )
			{
				// 重症度により判定を実施します。
//				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
//				{
//					if( iMinEmergency < ArrayListPatientAgents.get(i).iGetEmergencyLevel() )
//					{
//						iMinEmergency = ArrayListPatientAgents.get(i).iGetEmergencyLevel();
//						iTargetLoc = i;
//					}
//				}
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( lfMinAIS > ArrayListPatientAgents.get(i).lfGetMaxAIS() )
					{
						lfMinAIS = ArrayListPatientAgents.get(i).lfGetMaxAIS();
						iTargetLoc = i;
					}
				}
				for( i = 0;i < ArrayListNursePatientLoc.size(); i++ )
				{
					if( ArrayListNursePatientLoc.get( i ) == iTargetLoc )
					{
						iLoc = i;
					}
				}
				// 一般病棟へ移動します。
				vJudgeMoveGeneralWardRoom( ArrayListGeneralWardRooms, erDoctorAgent, ArrayListNurseAgents.get(iLoc), ArrayListPatientAgents.get(iTargetLoc), iLoc );
				bRet = true;
			}
		}
		return bRet;
	}

	/**
	 * <PRE>
	 *    空いている看護師を割り当てます。
	 * </PRE>
	 * @return false 割り当てることができませんでした。
	 *         true  割り当てることができました。
	 */
	public boolean bAssignVacantNurse()
	{
		int i,j;
		int iEnableNurse = 0;
		boolean bRet = false;
		for( i = 0 ;i < ArrayListNurseAgents.size(); i++ )
		{
//			cHighCareUnitLog.info("ArrayListNursePatientLoc Nurse:" + i + ",Patient:" + ArrayListNursePatientLoc.get(i) );
//			cHighCareUnitLog.info("ArrayListNursePatientLoc size:" + ArrayListNursePatientLoc.size());
//			cHighCareUnitLog.info("ArrayListPatientAgents size:" + ArrayListPatientAgents.size());
			// どの看護師が空いているかどうかを調べます。
			if( ArrayListNurseAgents.get(i).iGetAttending() == 0 )
			{
				// 空いている看護師に対して患者を割り振ります。
				iEnableNurse = i;
				for( j = 0;j < ArrayListPatientAgents.size(); j++ )
				{
					if( ArrayListPatientAgents.get(j).iGetNurseAttended() == 0 )
					{
						ArrayListNurseAgents.get(iEnableNurse).vSetAttending(1);
						ArrayListNursePatientLoc.set( iEnableNurse, j );
						ArrayListPatientAgents.get(j).vSetNurseAttended( 1 );
						ArrayListPatientAgents.get(j).vSetNurseAgent(ArrayListNurseAgents.get(i).getId());
						bRet = true;
						break;
					}
				}
			}
		}
		return bRet;
	}

	@Override
	public void action(long timeStep)
	{
		// TODO 自動生成されたメソッド・スタブ
		int i,j;
		int iLoc = 0;
		double lfSecond = 0.0;
		lfSecond = timeStep / 1000.0;
		lfTotalTime += lfSecond;
		// 死亡患者がいる場合は削除をする。
		if( ArrayListPatientAgents != null && ArrayListPatientAgents.size() > 0 )
		{
			for( i = ArrayListPatientAgents.size()-1; i >= 0; i-- )
			{
				if( ArrayListPatientAgents.get(i) != null )
				{
					if( ArrayListPatientAgents.get(i).iGetSurvivalFlag() == 0 || ArrayListPatientAgents.get(i).iGetDisChargeFlag() == 1 )
					{
						for( j = 0;j < ArrayListNursePatientLoc.size(); j++ )
						{
							if( ArrayListNursePatientLoc.get(j) == i )
							{
								cHighCareUnitLog.info(ArrayListPatientAgents.get(i).getId() + "," + "High Care Unit通ったよ～");
								ArrayListNursePatientLoc.set( j, -1 );
								iLoc = i;
							}
						}
						// なくなられたエージェントが配列上いた位置を削除するため、それ以降のデータがすべて1繰り下がるので、
						// それに対応する。そうしないと配列サイズを超えて参照したエラーが発生します。
						for( j = 0;j < ArrayListNursePatientLoc.size(); j++ )
						{
							if( iLoc < ArrayListNursePatientLoc.get(j) )
							{
								ArrayListNursePatientLoc.set( j, ArrayListNursePatientLoc.get(j)-1 );
							}
						}
						ArrayListPatientAgents.set(i, null);
						ArrayListPatientAgents.remove(i);

						break;
					}
				}
			}
		}
	}

	/**
	 * <PRE>
	 *    高度治療室のログ出力設定をします。
	 * </PRE>
	 * @param log	ロガークラスインスタンス
	 */
	public void vSetLog(Logger log)
	{
		// TODO 自動生成されたメソッド・スタブ
		cHighCareUnitLog = log;
	}

	/**
	 * <PRE>
	 *    HCUのX座標を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	X座標
	 */
	public int iGetX()
	{
		return iDrawX;
	}

	/**
	 * <PRE>
	 *    HCUのY座標を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	Y座標
	 */
	public int iGetY()
	{
		return iDrawY;
	}

	/**
	 * <PRE>
	 *    HCUの横幅を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	横幅
	 */
	public int iGetWidth()
	{
		return iDrawWidth;
	}

	/**
	 * <PRE>
	 *    HCUの縦幅を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	縦幅
	 */
	public int iGetHeight()
	{
		return iDrawHeight;
	}

	/**
	 * <PRE>
	 *    HCUの階数を取得します。
	 *    描画用です。
	 * </PRE>
	 * @return	階数
	 */
	public int iGetF()
	{
		return iDrawF;
	}

	/**
	 * <PRE>
	 *    HCUのX座標を格納します。
	 * </PRE>
	 * @param iData	X座標
	 */
	public void vSetX( int iData )
	{
		iDrawX = iData;
	}

	/**
	 * <PRE>
	 *    HCUのY座標を格納します。
	 * </PRE>
	 * @param iData	Y座標
	 */
	public void vSetY( int iData )
	{
		iDrawY = iData;
	}

	/**
	 * <PRE>
	 *    HCUのZ座標を格納します。
	 * </PRE>
	 * @param iData	Z座標
	 */
	public void vSetZ( int iData )
	{
		iDrawZ = iData;
	}

	/**
	 * <PRE>
	 *   HCUの横幅を格納します。
	 * </PRE>
	 * @param iData	横幅
	 */
	public void vSetWidth( int iData )
	{
		iDrawWidth = iData;
	}

	/**
	 * <PRE>
	 *    HCUの縦幅を格納します。
	 * </PRE>
	 * @param iData	縦幅
	 */
	public void vSetHeight( int iData )
	{
		iDrawHeight = iData;
	}

	/**
	 * <PRE>
	 *    HCUの階数を格納します。
	 * </PRE>
	 * @param iData	階数
	 */
	public void vSetF( int iData )
	{
		iDrawF = iData;
	}

	/**
	 * <PRE>
	 *   HCUに所属しているエージェントの座標を設定します。
	 * </PRE>
	 * @param iLoc			対象としている看護師の番号
	 * @param cCurNode		対象としている看護師のノード
	 */
	public void vSetAffiliationAgentPosition(ERTriageNode cCurNode, int iLoc)
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;

		double lfX = 0.0;
		double lfY = 0.0;
		double lfZ = 0.0;

		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
		{
			// 医師エージェントの位置を設定します。
			lfX = this.getPosition().getX()+3*rnd.NextUnif();
			lfY = this.getPosition().getY()+3*rnd.NextUnif();
			lfZ = this.getPosition().getZ()+3*rnd.NextUnif();
			ArrayListDoctorAgents.get(i).setPosition( lfX, lfY, lfZ );
		}
//		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
//		{
//			// 看護師エージェントの位置を設定します。
//			lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
//			lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
//			lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
//			ArrayListNurseAgents.get(i).setPosition( lfX, lfY, lfZ );
//		}
//		for( i = 0;i < ArrayListClinicalEngineerAgents.size(); i++ )
//		{
//			// 医療技師エージェントの位置を設定します。
//			lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
//			lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
//			lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
//			ArrayListClinicalEngineerAgents.get(i).setPosition( lfX, lfY, lfZ );
//		}
		if( iLoc < ArrayListNurseAgents.size() )
		{
			// 看護師エージェントの位置を設定します。
			lfX = cCurNode.getPosition().getX();
			lfY = cCurNode.getPosition().getY();
			lfZ = cCurNode.getPosition().getZ();
			ArrayListNurseAgents.get( iLoc ).setPosition( lfX, lfY, lfZ );
			ArrayListNurseAgents.get( iLoc ).vSetTriageNode( cCurNode );
		}
	}

	/**
	 * <PRE>
	 *    トリアージノードマネージャーを設定します。
	 * </PRE>
	 * @param erNodeManager	ノード、リンクが格納されたノードマネージャのインスタンス
	 */
	public void vSetERTriageNodeManager( ERTriageNodeManager erNodeManager )
	{
		erTriageNodeManager = erNodeManager;
	}

	/**
	 * <PRE>
	 *    現在選択されている高度治療室のノードを取得します。
	 * </PRE>
	 * @return	選択中の高度治療室のノード
	 */
	public ERTriageNode erGetTriageNode()
	{
		int i;
		ERTriageNode erNode = null;
		// 割り当てられていない看護師エージェントを調べて、そこに移動するように設定します。
		for( i = 0;i < ArrayListNursePatientLoc.size(); i++ )
		{
			if( ArrayListNursePatientLoc.get(i) == -1 )
			{
				erNode = ArrayListNurseAgents.get(i).erGetTriageNode();
				break;
			}
		}
		return erNode;
	}

	/**
	 * <PRE>
	 *   待合室のノードを設定します。
	 * </PRE>
	 * @param erNode	待合室のノード
	 */
	public void vSetTriageNode( ERTriageNode erNode )
	{
		erTriageNode = erNode;
	}

	/**
	 * <PRE>
	 *   逆シミュレーションモードを設定します。
	 * </PRE>
	 * @param iMode	0 通常シミュレーションモード
	 * 				1 GUIモード
	 * 				2 逆シミュレーションモード
	 */
	public void vSetInverseSimMode( int iMode )
	{
		int i;
		iInverseSimFlag = iMode;

		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
		{
			ArrayListDoctorAgents.get(i).vSetInverseSimMode( iMode );
		}

		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetInverseSimMode( iMode );
		}
	}

	/**
	 * <PRE>
	 *   患者エージェントオブジェクトを生成します。
	 * </PRE>
	 */
	public void vCreatePatientAgents()
	{
		ArrayListPatientAgents = new ArrayList<ERPatientAgent>();
	}


	/**
	 * <PRE>
	 *    現時点で患者がいるかどうかを取得します。
	 * </PRE>
	 * @return	現時点でHCUに在院している患者数
	 */
	public synchronized int iGetPatientInARoom()
	{
		// TODO 自動生成されたメソッド・スタブ
		int i;
		int iCount = 0;

		synchronized( csHighCareUnitCriticalSection )
		{
			if( ArrayListPatientAgents != null )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( ArrayListPatientAgents.get(i) != null )
					{
						if( ArrayListPatientAgents.get(i).iGetSurvivalFlag() == 1 )
						{
							iCount++;
						}
					}
				}
			}
		}
		return iCount;
	}

	/**
	 * <PRE>
	 *    クリティカルセクションを設定します。
	 * </PRE>
	 * @param cs	クリティカルセクションのインスタンス
	 */
	public void vSetCriticalSection(Object cs)
	{
		// TODO 自動生成されたメソッド・スタブ
		csHighCareUnitCriticalSection = cs;
	}

	/**
	 * <PRE>
	 *   メルセンヌツイスターインスタンスを設定します。
	 * </PRE>
	 * @param sfmtRandom メルセンヌツイスターインスタンス(部屋自体)
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetRandom(utility.sfmt.Rand sfmtRandom)
	{
		// TODO 自動生成されたメソッド・スタブ
		rnd = sfmtRandom;
	}

	/**
	 * <PRE>
	 *   メルセンヌツイスターインスタンスを設定します。
	 *   (室所属する医師エージェント)
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetDoctorsRandom()
	{
		int i;
		for( i = 0;i < ArrayListDoctorAgents.size(); i++ )
		{
			ArrayListDoctorAgents.get(i).vSetRandom( rnd );
		}
	}

	/**
	 * <PRE>
	 *   メルセンヌツイスターインスタンスを設定します。
	 *   (室所属する看護師エージェント)
	 * </PRE>
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public void vSetNursesRandom()
	{
		int i;
		for( i = 0;i < ArrayListNurseAgents.size(); i++ )
		{
			ArrayListNurseAgents.get(i).vSetRandom( rnd );
		}
	}

	/**
	 * <PRE>
	 *   HCUの患者エージェントの数を取得します。
	 *   nullでなければ1人いるので1と返却します。
	 * </PRE>
	 * @return	HCUにいる患者エージェントの総数
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public int iGetPatientAgentsNum()
	{
		int i;

		return ArrayListPatientAgents != null ? ArrayListPatientAgents.size() : 0;
	}

	/**
	 * <PRE>
	 *   現時点でのトリアージ緊急度別受診数の患者の数を求めます。
	 * </PRE>
	 * @param iCategory	トリアージ緊急度
	 * @return	緊急度別トリアージ受診人数
	 * @author kobayashi
	 * @since 2016/07/27
	 */
	public synchronized int iGetTriageCategoryPatientNum( int iCategory )
	{
		int i;
		int iCategoryPatientNum = 0;

		synchronized( csHighCareUnitCriticalSection )
		{
			if( ArrayListPatientAgents != null )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( iCategory == (ArrayListPatientAgents.get(i).iGetEmergencyLevel()-1) )
					{
						iCategoryPatientNum++;
					}
				}
			}
		}
		return iCategoryPatientNum;
	}

	/**
	 * <PRE>
	 *    現在、待合室に最も長く病院にいる患者の在院時間を取得します。
	 * </PRE>
	 * @return		最も長く病院に在院する患者の在院時間
	 */
	public synchronized double lfGetLongestStayPatient()
	{
		int i;
		double lfLongestStayTime = -Double.MAX_VALUE;

		synchronized( csHighCareUnitCriticalSection )
		{
			if( ArrayListPatientAgents != null )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( ArrayListPatientAgents.get(i).lfGetTimeCourse() > 0.0 )
					{
						if( lfLongestStayTime < ArrayListPatientAgents.get(i).lfGetTimeCourse() )
						{
							lfLongestStayTime = ArrayListPatientAgents.get(i).lfGetTimeCourse();
						}
					}
				}
			}
		}
		return lfLongestStayTime;
	}


	/**
	 * <PRE>
	 *    現在、最後に病床に入った患者の到着から入院までの時間を算出します。
	 * </PRE>
	 */
	public synchronized void vLastBedTime()
	{
		int i;
		double lfLongestTime = -Double.MAX_VALUE;
		double lfLastTime = -Double.MAX_VALUE;

		synchronized( csHighCareUnitCriticalSection )
		{
			if( ArrayListPatientAgents != null )
			{
				for( i = 0;i < ArrayListPatientAgents.size(); i++ )
				{
					if( ArrayListPatientAgents.get(i).lfGetTimeCourse() > 0.0 )
					{
						if( lfLongestTime < ArrayListPatientAgents.get(i).lfGetTotalTime() && ArrayListPatientAgents.get(i).lfGetHospitalStayTime() > 0.0 )
						{
							lfLongestTime = ArrayListPatientAgents.get(i).lfGetTotalTime();
							lfLastTime = ArrayListPatientAgents.get(i).lfGetTimeCourse()-ArrayListPatientAgents.get(i).lfGetHospitalStayTime();
						}
						// 入院していない場合は0とします。
						if( ArrayListPatientAgents.get(i).lfGetHospitalStayTime() == 0.0 )
						{
							lfLastTime = 0.0;
							lfLongestTime = 0.0;
						}
					}
				}
			}
			lfLongestTotalTime = lfLongestTime;
			lfLastBedTime = lfLastTime;
		}
	}

	/**
	 * <PRE>
	 *    現在最も長く病院にいる時間の総時間を返します。
	 * </PRE>
	 * @return	最も長く病院にいる時間の総時間
	 */
	public double lfGetLongestStayHospitalTotalTime()
	{
		return lfLongestTotalTime;
	}

	/**
	 * <PRE>
	 *    最後に入院と診断された患者の病院到達から入院までの時間を返します。
	 * </PRE>
	 * @return	最後に入院と診断された患者の病院到達から入院までの時間
	 */
	public double lfGetLastBedTime()
	{
		return lfLastBedTime;
	}

	/**
	 * <PRE>
	 *   HCUの患者を生成します。
	 * </PRE>
	 * @param erEngine			シミュレーションエンジン
	 * @param iRandomMode		傷病状態発生方法
	 * @param iInverseSimMode	シミュレーションモード
	 * @param iFileWriteMode	ファイル出力方法
	 * @param initparam			初期設定ファイルインスタンス
	 * @param cLog				ログ出力
	 * @param csObject			クリティカルセクション
	 * @throws IOException		ファイル出力例外
	 */
	public void vGeneratePatient(SimulationEngine erEngine, int iRandomMode, int iInverseSimMode, int iFileWriteMode, InitSimParam initparam, Logger cLog, Object csObject ) throws IOException
	{
		int i;
		double lfX,lfY,lfZ;
		double lfInitAIS = 0.0;
		double lfHospitalStayDay = 0.0;
		double lfAISRevisedSeries = 0.0;
	// 患者エージェント生成
		erEngine.pause();
		cHighCareUnitLog = cLog;
		ArrayListPatientAgents.add( new ERPatientAgent() );
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetLog( cLog );
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetCriticalSection( csObject );
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInitParam( initparam );
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetRandom( rnd );
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetPatientRandomMode( iRandomMode );
		// ランダムに患者の容体を割り当てます。
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1).vSetRandom();
//		engine.addAgent(ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ));
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1).vSetSimulationEngine(erEngine);
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveRoomFlag( 1 );
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetMoveWaitingTime( 181 );
		// 高度治療室なので7とします。
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetLocation( 7 );
		// 逆シミュレーションモードでなければ以下を実行します。
		iInverseSimFlag = iInverseSimMode;
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetInverseSimMode( iInverseSimFlag );
		// 通常モード及びGUIモードの場合ログ出力します。
		if( iInverseSimFlag == 0 || iInverseSimFlag == 1 )
		{
			ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetReadWriteFile( iFileWriteMode );
		}
	// 患者エージェントの位置を設定します。
		lfX = this.getPosition().getX()+10*(2*rnd.NextUnif()-1);
		lfY = this.getPosition().getY()+10*(2*rnd.NextUnif()-1);
		lfZ = this.getPosition().getZ()+10*(2*rnd.NextUnif()-1);
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).setPosition( lfX, lfY, lfZ );

	// 医師、看護師の割り当てを行います。
		if( ArrayListPatientAgents.isEmpty() == false )
		{
			// 各看護師の観察を実行します。
			for( i = 0 ;i < ArrayListNurseAgents.size(); i++ )
			{
				if( ArrayListNursePatientLoc.get(i) >= 0 )
				{
					// マルチスレッドによる齟齬への対処
					if( ArrayListPatientAgents.size() <= 0 ) 										continue;
					if( ArrayListPatientAgents.size() <= ArrayListNursePatientLoc.get(i) ) 			continue;
					if( ArrayListPatientAgents.get( ArrayListNursePatientLoc.get( i ) ) == null )	continue;

					// 医師エージェントの対応を実施します。
					erHighCareUnitDoctorAgent.vSetAttending( 1 );

					// 看護師エージェントの対応を実施します。
					ArrayListNurseAgents.get( i ).vSetAttending( 1 );

					// その患者を対応している医師、看護師エージェントのIDを設定します。
					ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetNurseAgent(ArrayListNurseAgents.get( i ).getId());
					ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetDoctorAgent(erHighCareUnitDoctorAgent.getId());
					break;
				}
			}
		}

	// 患者の改善度を算出します。

		// 入院時のAIS値を設定します。
		lfInitAIS = ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).lfSetHospitalStayInitAIS();

		// 入院日数を算出します。
		lfHospitalStayDay = lfCalcHospitalStay( ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ) );

		// 患者の入院日数を設定します。
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetHospitalStayDay( lfHospitalStayDay );

		// AIS値改善割合を算出します。
		double lfStepTime = this.getEngine().getLatestTimeStep()/1000.0;
		double lfStayTime = lfStepTime / (24.0*3600.0);
		lfAISRevisedSeries = lfCurePatientAISSeries( lfStayTime, lfHospitalStayDay, 0.5, lfInitAIS );

		// AIS値改善割合を設定します。
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetRevisedSeries( lfAISRevisedSeries );

		// 初めて高度治療室に入った場合に算出します。
		cHighCareUnitLog.info(ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).getId() + "," + erHighCareUnitDoctorAgent.getId() +"," + "高度治療室の平均在院日数を算出します。");
		// 平均在院日数を算出します。
		vCalcHcuAvgLengthOfStay();
		ArrayListPatientAgents.get( ArrayListPatientAgents.size()-1 ).vSetEnterHighCareUnitFlag( 1 );

		erEngine.resume();
		return;
	}
}
