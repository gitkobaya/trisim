package utility.initparam;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

import utility.initparam.initsettingfile.InitSettingFileRead;

public class InitInverseSimParam extends InitSettingFileRead
{
	private int iConsultationRoomNumMax;					// 診察室数の最大値
	private int iConsultationRoomNumMin;					// 診察室数の最小値
	private int iConsultationRoomDoctorNumMax;				// 1診察室に所属する医師の最大値
	private int iConsultationRoomDoctorNumMin;				// 1診察室に所属する医師の最小値
	private int iConsultationRoomNurseNumMax;				// 1診察室に所属する看護師の最大値
	private int iConsultationRoomNurseNumMin;				// 1診察室に所属する看護師の最小値
	private int iConsultationRoomTotalDoctorNum;			// 診察室に所属する医師の総数の基準値
	private int iConsultationRoomTotalNurseNum;				// 診察室に所属する看護師の総数の基準値
	private double iConsultationRoomDoctorNumMaxWeight;		// 診察室に所属する医師の最大範囲
	private double iConsultationRoomDoctorNumMinWeight;		// 診察室に所属する医師の最小範囲
	private double iConsultationRoomNurseNumMaxWeight;		// 診察室に所属する看護師の最大範囲
	private double iConsultationRoomNurseNumMinWeight;		// 診察室に所属する看護師の最小範囲

	private int iOperationRoomNumMax;						// 手術室数の最大値
	private int iOperationRoomNumMin;						// 手術室数の最小値
	private int iOperationRoomDoctorNumMax;					// 1手術室に所属する医師の最大値
	private int iOperationRoomDoctorNumMin;					// 1手術室に所属する医師の最小値
	private int iOperationRoomNurseNumMax;					// 1手術室に所属する看護師の最大値
	private int iOperationRoomNurseNumMin;					// 1手術室に所属する看護師の最小値
	private int iOperationRoomTotalDoctorNum;				// 手術室に所属する医師の総数の基準値
	private int iOperationRoomTotalNurseNum;				// 手術室に所属する看護師の総数の基準値
	private double iOperationRoomDoctorNumMaxWeight;		// 手術室に所属する医師の最大範囲
	private double iOperationRoomDoctorNumMinWeight;		// 手術室に所属する医師の最小範囲
	private double iOperationRoomNurseNumMaxWeight;			// 手術室に所属する看護師の最大範囲
	private double iOperationRoomNurseNumMinWeight;			// 手術室に所属する看護師の最小範囲

	private int iEmergencyRoomNumMax;						// 初療室数の最大値
	private int iEmergencyRoomNumMin;						// 初療室数の最小値
	private int iEmergencyRoomDoctorNumMax;					// 1初療室に所属する医師の最大値
	private int iEmergencyRoomDoctorNumMin;					// 1初療室に所属する医師の最小値
	private int iEmergencyRoomNurseNumMax;					// 1初療室に所属する看護師の最大値
	private int iEmergencyRoomNurseNumMin;					// 1初療室に所属する看護師の最小値
	private int iEmergencyRoomClinicalEngineerNumMax;		// 1初療室に所属する医療技師の最大値
	private int iEmergencyRoomClinicalEngineerNumMin;		// 1初療室に所属する医療技師の最小値
	private int iEmergencyRoomTotalDoctorNum;				// 初療室に所属する医師の総数の基準値
	private int iEmergencyRoomTotalNurseNum;				// 初療室に所属する看護師の総数の基準値
	private int iEmergencyRoomTotalClinicalEngineerNum;		// 初療室に所属する医療技師の総数の基準値
	private double iEmergencyRoomDoctorNumMaxWeight;		// 初療室に所属する医師の最大範囲
	private double iEmergencyRoomDoctorNumMinWeight;		// 初療室に所属する医師の最小範囲
	private double iEmergencyRoomNurseNumMaxWeight;			// 初療室に所属する看護師の最大範囲
	private double iEmergencyRoomNurseNumMinWeight;			// 初療室に所属する看護師の最小範囲

	private int iObservationRoomNumMax;						// 観察室数の最大値
	private int iObservationRoomNumMin;						// 観察室数の最小値
	private int iObservationRoomNurseNumMax;				// 1観察室に所属する看護師の最大値
	private int iObservationRoomNurseNumMin;				// 1観察室に所属する看護師の最小値
	private int iObservationRoomTotalNurseNum;				// 観察室に所属する看護師の総数の基準値
	private double iObservationRoomNurseNumMaxWeight;		// 観察室に所属する看護師の最大範囲
	private double iObservationRoomNurseNumMinWeight;		// 観察室に所属する看護師の最小範囲

	private int iSevereInjuryObservationRoomNumMax;					// 重症観察室数の最大値
	private int iSevereInjuryObservationRoomNumMin;					// 重症観察室数の最小値
	private int iSevereInjuryObservationRoomNurseNumMax;			// 1重症観察室に所属する看護師の最大値
	private int iSevereInjuryObservationRoomNurseNumMin;			// 1重症観察室に所属する看護師の最小値
	private int iSevereInjuryObservationRoomTotalNurseNum;			// 重症観察室に所属する看護師の総数の基準値
	private double iSevereInjuryObservationRoomNurseNumMaxWeight;	// 重症観察室に所属する看護師の最大範囲
	private double iSevereInjuryObservationRoomNurseNumMinWeight;	// 重症観察室に所属する看護師の最小範囲

	private int iIntensiveCareUnitNumMax;				// 集中治療室数の最大値
	private int iIntensiveCareUnitNumMin;				// 集中治療室数の最小値
	private int iIntensiveCareUnitDoctorNumMax;			// 1集中治療室に所属する医師の最大値
	private int iIntensiveCareUnitDoctorNumMin;			// 1集中治療室に所属する医師の最小値
	private int iIntensiveCareUnitNurseNumMax;			// 1集中治療室に所属する看護師の最大値
	private int iIntensiveCareUnitNurseNumMin;			// 1集中治療室に所属する看護師の最小値
	private int iIntensiveCareUnitTotalDoctorNum;		// 集中治療室に所属する医師の総数の基準値
	private int iIntensiveCareUnitTotalNurseNum;		// 集中治療室に所属する看護師の総数の基準値
	private double iIntensiveCareUnitDoctorNumMaxWeight;// 集中治療室に所属する医師の最大範囲
	private double iIntensiveCareUnitDoctorNumMinWeight;// 集中治療室に所属する医師の最小範囲
	private double iIntensiveCareUnitNurseNumMaxWeight;	// 集中治療室に所属する看護師の最大範囲
	private double iIntensiveCareUnitNurseNumMinWeight;	// 集中治療室に所属する看護師の最小範囲

	private int iHighCareUnitNumMax;					// HCU数の最大値
	private int iHighCareUnitNumMin;					// HCU数の最小値
	private int iHighCareUnitDoctorNumMax;				// 1HCUに所属する医師の最大値
	private int iHighCareUnitDoctorNumMin;				// 1HCUに所属する医師の最小値
	private int iHighCareUnitNurseNumMax;				// 1HCUに所属する看護師の最大値
	private int iHighCareUnitNurseNumMin;				// 1HCUに所属する看護師の最小値
	private int iHighCareUnitTotalDoctorNum;			// HCUに所属する医師の総数の基準値
	private int iHighCareUnitTotalNurseNum;				// HCUに所属する看護師の総数の基準値
	private double iHighCareUnitDoctorNumMaxWeight;		// HCUに所属する医師の最大範囲
	private double iHighCareUnitDoctorNumMinWeight;		// HCUに所属する医師の最小範囲
	private double iHighCareUnitNurseNumMaxWeight;		// HCUに所属する看護師の最大範囲
	private double iHighCareUnitNurseNumMinWeight;		// HCUに所属する看護師の最小範囲

	private int iGeneralWardNumMax;						// 一般病棟数の最大値
	private int iGeneralWardNumMin;						// 一般病棟数の最小値
	private int iGeneralWardDoctorNumMax;				// 1一般病棟に所属する医師の最大値
	private int iGeneralWardDoctorNumMin;				// 1一般病棟に所属する医師の最小値
	private int iGeneralWardNurseNumMax;				// 1一般病棟に所属する看護師の最大値
	private int iGeneralWardNurseNumMin;				// 1一般病棟に所属する看護師の最小値
	private int iGeneralWardTotalDoctorNum;				// 一般病棟に所属する医師の総数の基準値
	private int iGeneralWardTotalNurseNum;				// 一般病棟に所属する看護師の総数の基準値
	private double iGeneralWardDoctorNumMaxWeight;		// 一般病棟に所属する医師の最大範囲
	private double iGeneralWardDoctorNumMinWeight;		// 一般病棟に所属する医師の最小範囲
	private double iGeneralWardNurseNumMaxWeight;		// 一般病棟に所属する看護師の最大範囲
	private double iGeneralWardNurseNumMinWeight;		// 一般病棟に所属する看護師の最小範囲

	private int iWaitingRoomNumMax;						// 待合室数の最大値
	private int iWaitingRoomNumMin;						// 待合室数の最小値
	private int iWaitingRoomNurseNumMax;				// 1待合室に所属する看護師の最大値
	private int iWaitingRoomNurseNumMin;				// 1待合室に所属する看護師の最小値
	private int iWaitingRoomTotalNurseNum;				// 待合室に所属する看護師の総数の基準値
	private double iWaitingRoomNurseNumMaxWeight;		// 待合室に所属する看護師の最大範囲
	private double iWaitingRoomNurseNumMinWeight;		// 待合室に所属する看護師の最小範囲

	private int iXRayRoomNumMax;						// X線室数の最大値
	private int iXRayRoomNumMin;						// X線室数の最小値
	private int iXRayRoomClinicalEngineerNumMax;		// 1X線室に所属する医療技師の最大値
	private int iXRayRoomClinicalEngineerNumMin;		// 1X線室に所属する医療技師の最小値
	private int iXRayRoomTotalClinicalEngineerNum;		// X線室に所属する看護師の総数の基準値
	private double iXRayRoomClinicalEngineerNumMaxWeight;// X線室に所属する看護師の最大範囲
	private double iXRayRoomClinicalEngineerNumMinWeight;// X線室に所属する看護師の最小範囲

	private int iCTRoomNumMax;							// CT室数の最大値
	private int iCTRoomNumMin;							// CT室数の最小値
	private int iCTRoomClinicalEngineerNumMax;			// 1CT室に所属する医療技師の最大値
	private int iCTRoomClinicalEngineerNumMin;			// 1CT室に所属する医療技師の最小値
	private int iCTRoomTotalClinicalEngineerNum;		// CT室に所属する看護師の総数の基準値
	private double iCTRoomClinicalEngineerNumMaxWeight;	// CT室に所属する看護師の最大範囲
	private double iCTRoomClinicalEngineerNumMinWeight;	// CT室に所属する看護師の最小範囲

	private int iMRIRoomNumMax;							// MRI室数の最大値
	private int iMRIRoomNumMin;							// MRI室数の最小値
	private int iMRIRoomClinicalEngineerNumMax;			// 1MRI室に所属する医療技師の最大値
	private int iMRIRoomClinicalEngineerNumMin;			// 1MRI室に所属する医療技師の最小値
	private int iMRIRoomTotalClinicalEngineerNum;		// MRI室に所属する看護師の総数の基準値
	private double iMRIRoomClinicalEngineerNumMaxWeight;// MRI室に所属する看護師の最大範囲
	private double iMRIRoomClinicalEngineerNumMinWeight;// MRI室に所属する看護師の最小範囲

	private int iAngiographyRoomNumMax;							// 血管造影室数の最大値
	private int iAngiographyRoomNumMin;							// 血管造影室数の最小値
	private int iAngiographyRoomClinicalEngineerNumMax;			// 1血管造影室に所属する医療技師の最大値
	private int iAngiographyRoomClinicalEngineerNumMin;			// 1血管造影室に所属する医療技師の最小値
	private int iAngiographyRoomTotalClinicalEngineerNum;		// 血管造影室に所属する看護師の総数の基準値
	private double iAngiographyRoomClinicalEngineerNumMaxWeight;// 血管造影室に所属する看護師の最大範囲
	private double iAngiographyRoomClinicalEngineerNumMinWeight;// 血管造影室に所属する看護師の最小範囲

	private int iFastRoomNumMax;								// FAST室数の最大値
	private int iFastRoomNumMin;								// FAST室数の最小値
	private int iFastRoomClinicalEngineerNumMax;				// 1FAST室に所属する医療技師の最大値
	private int iFastRoomClinicalEngineerNumMin;				// 1FAST室に所属する医療技師の最小値
	private int iFastRoomTotalClinicalEngineerNum;				// FAST室に所属する看護師の総数の基準値
	private double iFastRoomClinicalEngineerNumMaxWeight;		// FAST室に所属する看護師の最大範囲
	private double iFastRoomClinicalEngineerNumMinWeight;		// FAST室に所属する看護師の最小範囲

	/**
	 * <PRE>
	 *	*.iniファイルに設定するパラメーターのデフォルト値を設定します。
	 * </PRE>
	 *
	 *	@author kobayashi
	 *	@since	0.1 2017/3/2
	 *	@version 0.1
	 */
	public void vSetDefaultValue()
	{
		// デフォルト値
	}

	@Override
	public void readInitSettingFile() throws IllegalArgumentException, IOException
	{
		// TODO 自動生成されたメソッド・スタブ
		String func_name				= "GetEnvParameter";
		int i = 0;
		String strIniFullPath;
		String strIniFileName								= "erInvSim.ini";
		String strConsultationRoomSectionName				= "ConsultationRoom";
		String strOperationRoomSectionName					= "OperationRoom";
		String strEmergencyRoomSectionName					= "EmergencyRoom";
		String strObservationRoomSectionName				= "ObservationRoom";
		String strSevereInjuryObservationRoomSectionName	= "SevereInjuryObservationRoom";
		String strIntensiveCareUnitSectionName				= "IntensiveCareUnit";
		String strHighCareUnitSectionName					= "HighCareUnit";
		String strGeneralWardSectionName					= "GeneralWard";
		String strWaitingRoomSectionName					= "WaitingRoom";
		String strXRayRoomSectionName						= "XRayRoom";
		String strCTRoomSectionName							= "CTRoom";
		String strMRIRoomSectionName						= "MRIRoom";
		String strAngiographyRoomSectionName				= "AngiographyRoom";
		String strFastRoomSectionName						= "FastRoom";
		String strParam;

		long lRet = 0L;
		File file;

		file = new File( strIniFileName );

	// 制約条件設定

		strIniFullPath = file.getAbsolutePath( );

		// 診察室数の最小値を取得します。
		iConsultationRoomNumMin = (int)GetInitDataInt( strConsultationRoomSectionName, "ConsultationRoomMin", -1, strIniFullPath );
		if( 0 > iConsultationRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of consultation room number is out of range"));
		}
		// 診察室数の最大値を取得します。
		iConsultationRoomNumMax = (int)GetInitDataInt( strConsultationRoomSectionName, "ConsultationRoomMax", -1, strIniFullPath );
		if( 0 > iConsultationRoomNumMin || iConsultationRoomNumMax < iConsultationRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of consultation room number is out of range"));
		}
		// 1診察室の医師の最小値を取得します。
		iConsultationRoomDoctorNumMin = (int)GetInitDataInt( strConsultationRoomSectionName, "ConsultationRoomDoctorMin", -1, strIniFullPath );
		if( 0 > iConsultationRoomDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of doctor in the consultation room number is out of range"));
		}
		// 1診察室の医師の最大値を取得します。
		iConsultationRoomDoctorNumMax = (int)GetInitDataInt( strConsultationRoomSectionName, "ConsultationRoomDoctorMax", -1, strIniFullPath );
		if( 0 > iConsultationRoomDoctorNumMax || iConsultationRoomDoctorNumMax < iConsultationRoomDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of doctor in the consultation room is out of range"));
		}
		// 1診察室の看護師の最小値を取得します。
		iConsultationRoomNurseNumMin = (int)GetInitDataInt( strConsultationRoomSectionName, "ConsultationRoomNurseMin", -1, strIniFullPath );
		if( 0 > iConsultationRoomNurseNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of nurse in the consultation room number is out of range"));
		}
		// 1診察室の看護師の最大値を取得します。
		iConsultationRoomNurseNumMax = (int)GetInitDataInt( strConsultationRoomSectionName, "ConsultationRoomNurseMax", -1, strIniFullPath );
		if( 0 > iConsultationRoomNurseNumMax || iConsultationRoomNurseNumMax < iConsultationRoomNurseNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of nurse in the consultation room is out of range"));
		}
		// 診察室の医師の総数の基準値を取得します。
		iConsultationRoomTotalDoctorNum = (int)GetInitDataInt( strConsultationRoomSectionName, "ConsultationRoomTotalDoctor", -1, strIniFullPath );
		if( 0 > iConsultationRoomTotalDoctorNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total doctor in the consultation room number is out of range"));
		}
		// 診察室の看護師の総数の基準値を取得します。
		iConsultationRoomTotalNurseNum = (int)GetInitDataInt( strConsultationRoomSectionName, "ConsultationRoomTotalNurse", -1, strIniFullPath );
		if( 0 > iConsultationRoomTotalNurseNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total nurse in the consultation room number is out of range"));
		}
		// 1診察室の医師の最小範囲を取得します。
		iConsultationRoomDoctorNumMinWeight = GetInitDataFloat( strConsultationRoomSectionName, "StandardConsultationRoomDoctorMinWeight", -1, strIniFullPath );
		if( 0 > iConsultationRoomDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of doctor in the consultation room number is out of range"));
		}
		// 1診察室の医師の最大範囲を取得します。
		iConsultationRoomDoctorNumMaxWeight = GetInitDataFloat( strConsultationRoomSectionName, "StandardConsultationRoomDoctorMaxWeight", -1, strIniFullPath );
		if( 0 > iConsultationRoomDoctorNumMaxWeight || iConsultationRoomDoctorNumMaxWeight < iConsultationRoomDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of doctor in the consultation room is out of range"));
		}
		// 1診察室の看護師数の最小範囲を取得します。
		iConsultationRoomNurseNumMinWeight = GetInitDataFloat( strConsultationRoomSectionName, "StandardConsultationRoomNurseMinWeight", -1, strIniFullPath );
		if( 0 > iConsultationRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of nurse in the waiting room number is out of range"));
		}
		// 1診察室の看護師数の最大範囲を取得します。
		iConsultationRoomNurseNumMaxWeight = GetInitDataFloat( strConsultationRoomSectionName, "StandardConsultationRoomNurseMaxWeight", -1, strIniFullPath );
		if( 0 > iConsultationRoomNurseNumMaxWeight || iConsultationRoomNurseNumMaxWeight < iConsultationRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of nurse in the consultation room is out of range"));
		}

		// 手術室数の最小値を取得します。
		iOperationRoomNumMin = (int)GetInitDataInt( strOperationRoomSectionName, "OperationRoomMin", -1, strIniFullPath );
		if( 0 > iOperationRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of operation room number is out of range"));
		}
		// 手術室数の最大値を取得します。
		iOperationRoomNumMax = (int)GetInitDataInt( strOperationRoomSectionName, "OperationRoomMax", -1, strIniFullPath );
		if( 0 > iOperationRoomNumMin || iOperationRoomNumMax < iOperationRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of operation room number is out of range"));
		}
		// 1手術室の医師の最小値を取得します。
		iOperationRoomDoctorNumMin = (int)GetInitDataInt( strOperationRoomSectionName, "OperationRoomDoctorMin", -1, strIniFullPath );
		if( 0 > iOperationRoomDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of doctor in the operation room number is out of range"));
		}
		// 1手術室の医師の最大値を取得します。
		iOperationRoomDoctorNumMax = (int)GetInitDataInt( strOperationRoomSectionName, "OperationRoomDoctorMax", -1, strIniFullPath );
		if( 0 > iOperationRoomDoctorNumMax || iOperationRoomDoctorNumMax < iOperationRoomDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of doctor in the operation room is out of range"));
		}
		// 1手術室の看護師の最小値を取得します。
		iOperationRoomNurseNumMin = (int)GetInitDataInt( strOperationRoomSectionName, "OperationRoomNurseMin", -1, strIniFullPath );
		if( 0 > iOperationRoomNurseNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of nurse in the operation room number is out of range"));
		}
		// 1手術室の看護師の最大値を取得します。
		iOperationRoomNurseNumMax = (int)GetInitDataInt( strOperationRoomSectionName, "OperationRoomNurseMax", -1, strIniFullPath );
		if( 0 > iOperationRoomNurseNumMax || iOperationRoomNurseNumMax < iOperationRoomNurseNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of nurse in the operation room is out of range"));
		}
		// 手術室の医師の総数の基準値を取得します。
		iOperationRoomTotalDoctorNum = (int)GetInitDataInt( strOperationRoomSectionName, "OperationRoomTotalDoctor", -1, strIniFullPath );
		if( 0 > iOperationRoomTotalDoctorNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total doctor in the operation room number is out of range"));
		}
		// 手術室の看護師の総数の基準値を取得します。
		iOperationRoomTotalNurseNum = (int)GetInitDataInt( strOperationRoomSectionName, "OperationRoomTotalNurse", -1, strIniFullPath );
		if( 0 > iOperationRoomTotalNurseNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total nurse in the operation room number is out of range"));
		}
		// 1手術室の看護師数の最小範囲を取得します。
		iOperationRoomNurseNumMinWeight = GetInitDataFloat( strOperationRoomSectionName, "StandardOperationRoomNurseMinWeight", -1, strIniFullPath );
		if( 0 > iOperationRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of nurse in the operation room number is out of range"));
		}
		// 1手術室の看護師数の最大範囲を取得します。
		iOperationRoomNurseNumMaxWeight = GetInitDataFloat( strOperationRoomSectionName, "StandardOperationRoomNurseMaxWeight", -1, strIniFullPath );
		if( 0 > iOperationRoomNurseNumMaxWeight || iOperationRoomNurseNumMaxWeight < iOperationRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of nurse in the operation room is out of range"));
		}
		// 1手術室の医師の最小範囲を取得します。
		iOperationRoomDoctorNumMinWeight = GetInitDataFloat( strOperationRoomSectionName, "StandardOperationRoomDoctorMinWeight", -1, strIniFullPath );
		if( 0 > iOperationRoomDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of doctor in the operation room number is out of range"));
		}
		// 1手術室の医師の最大範囲を取得します。
		iOperationRoomDoctorNumMaxWeight = GetInitDataFloat( strOperationRoomSectionName, "StandardOperationRoomDoctorMaxWeight", -1, strIniFullPath );
		if( 0 > iOperationRoomDoctorNumMaxWeight || iOperationRoomDoctorNumMaxWeight < iOperationRoomDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of doctor in the operation room is out of range"));
		}

		// 初療室数の最小値を取得します。
		iEmergencyRoomNumMin = (int)GetInitDataInt( strEmergencyRoomSectionName, "EmergencyRoomMin", -1, strIniFullPath );
		if( 0 > iEmergencyRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of emergency room number is out of range"));
		}
		// 初療室数の最大値を取得します。
		iEmergencyRoomNumMax = (int)GetInitDataInt( strEmergencyRoomSectionName, "EmergencyRoomMax", -1, strIniFullPath );
		if( 0 > iEmergencyRoomNumMin || iEmergencyRoomNumMax < iEmergencyRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of emergency room number is out of range"));
		}
		// 1初療室の医師の最小値を取得します。
		iEmergencyRoomDoctorNumMin = (int)GetInitDataInt( strEmergencyRoomSectionName, "EmergencyRoomDoctorMin", -1, strIniFullPath );
		if( 0 > iEmergencyRoomDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of doctor in the emergency room number is out of range"));
		}
		// 1初療室の医師の最大値を取得します。
		iEmergencyRoomDoctorNumMax = (int)GetInitDataInt( strEmergencyRoomSectionName, "EmergencyRoomDoctorMax", -1, strIniFullPath );
		if( 0 > iEmergencyRoomDoctorNumMax || iEmergencyRoomDoctorNumMax < iEmergencyRoomDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of doctor in the emergency room is out of range"));
		}
		// 1初療室の看護師の最小値を取得します。
		iEmergencyRoomNurseNumMin = (int)GetInitDataInt( strEmergencyRoomSectionName, "EmergencyRoomNurseMin", -1, strIniFullPath );
		if( 0 > iEmergencyRoomNurseNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of nurse in the emergency room number is out of range"));
		}
		// 1初療室の看護師の最大値を取得します。
		iEmergencyRoomNurseNumMax = (int)GetInitDataInt( strEmergencyRoomSectionName, "EmergencyRoomNurseMax", -1, strIniFullPath );
		if( 0 > iEmergencyRoomNurseNumMax || iEmergencyRoomNurseNumMax < iEmergencyRoomNurseNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of nurse in the emergency room is out of range"));
		}
		// 初療室の医師の総数の基準値を取得します。
		iEmergencyRoomTotalDoctorNum = (int)GetInitDataInt( strEmergencyRoomSectionName, "EmergencyRoomTotalDoctor", -1, strIniFullPath );
		if( 0 > iEmergencyRoomTotalDoctorNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total doctor in the emergency room number is out of range"));
		}
		// 初療室の看護師の総数の基準値を取得します。
		iEmergencyRoomTotalNurseNum = (int)GetInitDataInt( strEmergencyRoomSectionName, "EmergencyRoomTotalNurse", -1, strIniFullPath );
		if( 0 > iEmergencyRoomTotalNurseNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total nurse in the emergency room number is out of range"));
		}
		// 1初療室の看護師数の最小範囲を取得します。
		iEmergencyRoomNurseNumMinWeight = GetInitDataFloat( strEmergencyRoomSectionName, "StandardEmergencyRoomNurseMinWeight", -1, strIniFullPath );
		if( 0 > iEmergencyRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of nurse in the emergency room number is out of range"));
		}
		// 1初療室の看護師数の最大範囲を取得します。
		iEmergencyRoomNurseNumMaxWeight = GetInitDataFloat( strEmergencyRoomSectionName, "StandardEmergencyRoomNurseMaxWeight", -1, strIniFullPath );
		if( 0 > iEmergencyRoomNurseNumMaxWeight || iEmergencyRoomNurseNumMaxWeight < iEmergencyRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of nurse in the emergency room is out of range"));
		}
		// 1初療室の医師の最小範囲を取得します。
		iEmergencyRoomDoctorNumMinWeight = GetInitDataFloat( strEmergencyRoomSectionName, "StandardEmergencyRoomDoctorMinWeight", -1, strIniFullPath );
		if( 0 > iEmergencyRoomDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of doctor in the emergency room number is out of range"));
		}
		// 1初療室の医師の最大範囲を取得します。
		iEmergencyRoomDoctorNumMaxWeight = GetInitDataFloat( strEmergencyRoomSectionName, "StandardEmergencyRoomDoctorMaxWeight", -1, strIniFullPath );
		if( 0 > iEmergencyRoomDoctorNumMaxWeight || iEmergencyRoomDoctorNumMaxWeight < iEmergencyRoomDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of doctor in the emergency room is out of range"));
		}

		// 観察室数の最小値を取得します。
		iObservationRoomNumMin = (int)GetInitDataInt( strObservationRoomSectionName, "ObservationRoomMin", -1, strIniFullPath );
		if( 0 > iObservationRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of observation room number is out of range"));
		}
		// 観察室数の最大値を取得します。
		iObservationRoomNumMax = (int)GetInitDataInt( strObservationRoomSectionName, "ObservationRoomMax", -1, strIniFullPath );
		if( 0 > iObservationRoomNumMin || iObservationRoomNumMax < iObservationRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of observation room number is out of range"));
		}
		// 1観察室の看護師の最小値を取得します。
		iObservationRoomNurseNumMin = (int)GetInitDataInt( strObservationRoomSectionName, "ObservationRoomNurseMin", -1, strIniFullPath );
		if( 0 > iObservationRoomNurseNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of nurse in the observation room number is out of range"));
		}
		// 1観察室の看護師の最大値を取得します。
		iObservationRoomNurseNumMax = (int)GetInitDataInt( strObservationRoomSectionName, "ObservationRoomNurseMax", -1, strIniFullPath );
		if( 0 > iObservationRoomNurseNumMax || iObservationRoomNurseNumMax < iObservationRoomNurseNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of nurse in the observation room is out of range"));
		}
		// 観察室の看護師の総数の基準値を取得します。
		iObservationRoomTotalNurseNum = (int)GetInitDataInt( strObservationRoomSectionName, "ObservationRoomTotalNurse", -1, strIniFullPath );
		if( 0 > iObservationRoomTotalNurseNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total nurse in the observation room number is out of range"));
		}
		// 1観察室の看護師数の最小範囲を取得します。
		iObservationRoomNurseNumMinWeight = GetInitDataFloat( strObservationRoomSectionName, "StandardObservationRoomNurseMinWeight", -1, strIniFullPath );
		if( 0 > iObservationRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of nurse in the observation room number is out of range"));
		}
		// 1観察室の看護師数の最大範囲を取得します。
		iObservationRoomNurseNumMaxWeight = GetInitDataFloat( strObservationRoomSectionName, "StandardObservationRoomNurseMaxWeight", -1, strIniFullPath );
		if( 0 > iObservationRoomNurseNumMaxWeight || iObservationRoomNurseNumMaxWeight < iObservationRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of nurse in the observation room is out of range"));
		}

		// 重症観察室数の最小値を取得します。
		iSevereInjuryObservationRoomNumMin = (int)GetInitDataInt( strSevereInjuryObservationRoomSectionName, "SevereInjuryObservationRoomMin", -1, strIniFullPath );
		if( 0 > iSevereInjuryObservationRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of  severe injury observation room number is out of range"));
		}
		// 重症観察室数の最大値を取得します。
		iSevereInjuryObservationRoomNumMax = (int)GetInitDataInt( strSevereInjuryObservationRoomSectionName, "SevereInjuryObservationRoomMax", -1, strIniFullPath );
		if( 0 > iSevereInjuryObservationRoomNumMin || iSevereInjuryObservationRoomNumMax < iSevereInjuryObservationRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of severe injury observation room number is out of range"));
		}
		// 1重症観察室の看護師の最小値を取得します。
		iSevereInjuryObservationRoomNurseNumMin = (int)GetInitDataInt( strSevereInjuryObservationRoomSectionName, "SevereInjuryObservationRoomNurseMin", -1, strIniFullPath );
		if( 0 > iSevereInjuryObservationRoomNurseNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of nurse in the severe injury observation room number is out of range"));
		}
		// 1重症観察室の看護師の最大値を取得します。
		iSevereInjuryObservationRoomNurseNumMax = (int)GetInitDataInt( strSevereInjuryObservationRoomSectionName, "SevereInjuryObservationRoomNurseMax", -1, strIniFullPath );
		if( 0 > iSevereInjuryObservationRoomNurseNumMax || iSevereInjuryObservationRoomNurseNumMax < iSevereInjuryObservationRoomNurseNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of nurse in the severe injury observation room is out of range"));
		}
		// 重症観察室の看護師の総数の基準値を取得します。
//		iSevereInjuryObservationRoomTotalNurseNum = (int)GetInitDataInt( strSevereInjuryObservationRoomSectionName, "SevereInjuryObservationRoomTotalNurse", 25, strIniFullPath );
//		if( 0 > iSevereInjuryObservationRoomTotalNurseNum )
//		{
//			/* 範囲外を指定した場合はエラーを出力します。*/
//			throw(new IllegalArgumentException("value of total nurse in the severe injury observation room number is out of range\n"));
//		}
		// 1重症観察室の看護師数の最小範囲を取得します。
		iSevereInjuryObservationRoomNurseNumMinWeight = GetInitDataFloat( strSevereInjuryObservationRoomSectionName, "StandardSevereInjuryObservationRoomNurseMinWeight", -1, strIniFullPath );
		if( 0 > iSevereInjuryObservationRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of nurse in the severe injury observation room number is out of range"));
		}
		// 1重症観察室の看護師数の最大範囲を取得します。
		iSevereInjuryObservationRoomNurseNumMaxWeight = GetInitDataFloat( strSevereInjuryObservationRoomSectionName, "StandardSevereInjuryObservationRoomNurseMaxWeight", -1, strIniFullPath );
		if( 0 > iSevereInjuryObservationRoomNurseNumMaxWeight || iSevereInjuryObservationRoomNurseNumMaxWeight < iSevereInjuryObservationRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of nurse in the severe injury observation room is out of range"));
		}

		// ICU数の最小値を取得します。
		iIntensiveCareUnitNumMin = (int)GetInitDataInt( strIntensiveCareUnitSectionName, "IntensiveCareUnitMin", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of intensive care unit number is out of range"));
		}
		// ICU数の最大値を取得します。
		iIntensiveCareUnitNumMax = (int)GetInitDataInt( strIntensiveCareUnitSectionName, "IntensiveCareUnitMax", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitNumMin || iIntensiveCareUnitNumMax < iIntensiveCareUnitNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of intensive care unit number is out of range"));
		}
		// 1ICUの医師の最小値を取得します。
		iIntensiveCareUnitDoctorNumMin = (int)GetInitDataInt( strIntensiveCareUnitSectionName, "IntensiveCareUnitDoctorMin", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of doctor in the intensive care unit number is out of range"));
		}
		// 1ICUの医師の最大値を取得します。
		iIntensiveCareUnitDoctorNumMax = (int)GetInitDataInt( strIntensiveCareUnitSectionName, "IntensiveCareUnitDoctorMax", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitDoctorNumMax || iIntensiveCareUnitDoctorNumMax < iIntensiveCareUnitDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of doctor in the intensive care unit is out of range"));
		}
		// 1ICUの看護師の最小値を取得します。
		iIntensiveCareUnitNurseNumMin = (int)GetInitDataInt( strIntensiveCareUnitSectionName, "IntensiveCareUnitNurseMin", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitNurseNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of nurse in the intensive care unit number is out of range"));
		}
		// 1ICUの看護師の最大値を取得します。
		iIntensiveCareUnitNurseNumMax = (int)GetInitDataInt( strIntensiveCareUnitSectionName, "IntensiveCareUnitNurseMax", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitNurseNumMax || iIntensiveCareUnitNurseNumMax < iIntensiveCareUnitNurseNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of nurse in the intensive care unit is out of range"));
		}
		// ICUの医師の総数の基準値を取得します。
		iIntensiveCareUnitTotalDoctorNum = (int)GetInitDataInt( strIntensiveCareUnitSectionName, "IntensiveCareUnitTotalDoctor", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitTotalDoctorNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total doctor in the intensive care unit number is out of range"));
		}
		// ICUの看護師の総数の基準値を取得します。
		iIntensiveCareUnitTotalNurseNum = (int)GetInitDataInt( strIntensiveCareUnitSectionName, "IntensiveCareUnitTotalNurse", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitTotalNurseNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total nurse in the intensive care unit number is out of range"));
		}
		// ICUの看護師数の最小範囲を取得します。
		iIntensiveCareUnitNurseNumMinWeight = GetInitDataFloat( strIntensiveCareUnitSectionName, "StandardIntensiveCareUnitNurseMinWeight", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of nurse in the intensive care unit number is out of range"));
		}
		// ICUの看護師数の最大範囲を取得します。
		iIntensiveCareUnitNurseNumMaxWeight = GetInitDataFloat( strIntensiveCareUnitSectionName, "StandardIntensiveCareUnitNurseMaxWeight", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitNurseNumMaxWeight || iIntensiveCareUnitNurseNumMaxWeight < iIntensiveCareUnitNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of nurse in the intensive care unit is out of range"));
		}
		// ICUの医師の最小範囲を取得します。
		iIntensiveCareUnitDoctorNumMinWeight = GetInitDataFloat( strIntensiveCareUnitSectionName, "StandardIntensiveCareUnitDoctorMinWeight", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of doctor in the intensive care unit number is out of range"));
		}
		// ICUの医師の最大範囲を取得します。
		iIntensiveCareUnitDoctorNumMaxWeight = GetInitDataFloat( strIntensiveCareUnitSectionName, "StandardIntensiveCareUnitDoctorMaxWeight", -1, strIniFullPath );
		if( 0 > iIntensiveCareUnitDoctorNumMaxWeight || iIntensiveCareUnitDoctorNumMaxWeight < iIntensiveCareUnitDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of doctor in the intensive care unit is out of range"));
		}

		// HCU数の最小値を取得します。
		iHighCareUnitNumMin = (int)GetInitDataInt( strHighCareUnitSectionName, "HighCareUnitMin", -1, strIniFullPath );
		if( 0 > iHighCareUnitNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of high care unit number is out of range"));
		}
		// HCU数の最大値を取得します。
		iHighCareUnitNumMax = (int)GetInitDataInt( strHighCareUnitSectionName, "HighCareUnitMax", -1, strIniFullPath );
		if( 0 > iHighCareUnitNumMin || iHighCareUnitNumMax < iHighCareUnitNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of cohigh care unitumber is out of range"));
		}
		// 1HCUの医師の最小値を取得します。
		iHighCareUnitDoctorNumMin = (int)GetInitDataInt( strHighCareUnitSectionName, "HighCareUnitDoctorMin", -1, strIniFullPath );
		if( 0 > iHighCareUnitDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of doctor in the high care unit number is out of range"));
		}
		// 1HCUの医師の最大値を取得します。
		iHighCareUnitDoctorNumMax = (int)GetInitDataInt( strHighCareUnitSectionName, "HighCareUnitDoctorMax", -1, strIniFullPath );
		if( 0 > iHighCareUnitDoctorNumMax || iHighCareUnitDoctorNumMax < iHighCareUnitDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of doctor in the high care unit is out of range"));
		}
		// 1HCUの看護師の最小値を取得します。
		iHighCareUnitNurseNumMin = (int)GetInitDataInt( strHighCareUnitSectionName, "HighCareUnitNurseMin", -1, strIniFullPath );
		if( 0 > iHighCareUnitNurseNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of nurse in the high care unit number is out of range"));
		}
		// 1HCUの看護師の最大値を取得します。
		iHighCareUnitNurseNumMax = (int)GetInitDataInt( strHighCareUnitSectionName, "HighCareUnitNurseMax", -1, strIniFullPath );
		if( 0 > iHighCareUnitNurseNumMax || iHighCareUnitNurseNumMax < iHighCareUnitNurseNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of nurse in the high care unit is out of range"));
		}
		// HCUの医師の総数の基準値を取得します。
		iHighCareUnitTotalDoctorNum = (int)GetInitDataInt( strHighCareUnitSectionName, "HighCareUnitTotalDoctor", -1, strIniFullPath );
		if( 0 > iHighCareUnitTotalDoctorNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total doctor in the high care unit number is out of range"));
		}
		// HCUの看護師の総数の基準値を取得します。
		iHighCareUnitTotalNurseNum = (int)GetInitDataInt( strHighCareUnitSectionName, "HighCareUnitTotalNurse", -1, strIniFullPath );
		if( 0 > iHighCareUnitTotalNurseNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total nurse in the high care unit number is out of range"));
		}
		// HCUの看護師数の最小範囲を取得します。
		iHighCareUnitNurseNumMinWeight = GetInitDataFloat( strHighCareUnitSectionName, "StandardHighCareUnitNurseMinWeight", -1, strIniFullPath );
		if( 0 > iHighCareUnitNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of nurse in the high care unit number is out of range"));
		}
		// HCUの看護師数の最大範囲を取得します。
		iHighCareUnitNurseNumMaxWeight = GetInitDataFloat( strHighCareUnitSectionName, "StandardHighCareUnitNurseMaxWeight", -1, strIniFullPath );
		if( 0 > iHighCareUnitNurseNumMaxWeight || iHighCareUnitNurseNumMaxWeight < iHighCareUnitNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of nurse in the high care unit is out of range"));
		}
		// HCUの医師の最小範囲を取得します。
		iHighCareUnitDoctorNumMinWeight = GetInitDataFloat( strHighCareUnitSectionName, "StandardHighCareUnitDoctorMinWeight", -1, strIniFullPath );
		if( 0 > iHighCareUnitDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of doctor in the high care unit number is out of range"));
		}
		// HCUの医師の最大範囲を取得します。
		iHighCareUnitDoctorNumMaxWeight = GetInitDataFloat( strHighCareUnitSectionName, "StandardHighCareUnitDoctorMaxWeight", -1, strIniFullPath );
		if( 0 > iHighCareUnitDoctorNumMaxWeight || iHighCareUnitDoctorNumMaxWeight < iHighCareUnitDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of doctor in the high care unit is out of range"));
		}

		// 一般病棟数の最小値を取得します。
		iGeneralWardNumMin = (int)GetInitDataInt( strGeneralWardSectionName, "GeneralWardMin", -1, strIniFullPath );
		if( 0 > iGeneralWardNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of general ward number is out of range"));
		}
		// 一般病棟数の最大値を取得します。
		iGeneralWardNumMax = (int)GetInitDataInt( strGeneralWardSectionName, "GeneralWardMax", -1, strIniFullPath );
		if( 0 > iGeneralWardNumMin || iGeneralWardNumMax < iGeneralWardNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value general wardoom number is out of range"));
		}
		// 1一般病棟の医師の最小値を取得します。
		iGeneralWardDoctorNumMin = (int)GetInitDataInt( strGeneralWardSectionName, "GeneralWardDoctorMin", -1, strIniFullPath );
		if( 0 > iGeneralWardDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of doctor in the general ward number is out of range"));
		}
		// 1一般病棟の医師の最大値を取得します。
		iGeneralWardDoctorNumMax = (int)GetInitDataInt( strGeneralWardSectionName, "GeneralWardDoctorMax", -1, strIniFullPath );
		if( 0 > iGeneralWardDoctorNumMax || iGeneralWardDoctorNumMax < iGeneralWardDoctorNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of doctor in the general ward is out of range"));
		}
		// 1一般病棟の看護師の最小値を取得します。
		iGeneralWardNurseNumMin = (int)GetInitDataInt( strGeneralWardSectionName, "GeneralWardNurseMin", -1, strIniFullPath );
		if( 0 > iGeneralWardNurseNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of nurse in the general ward number is out of range"));
		}
		// 1一般病棟の看護師の最大値を取得します。
		iGeneralWardNurseNumMax = (int)GetInitDataInt( strGeneralWardSectionName, "GeneralWardNurseMax", -1, strIniFullPath );
		if( 0 > iGeneralWardNurseNumMax || iGeneralWardNurseNumMax < iGeneralWardNurseNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of nurse in the general ward is out of range"));
		}
		// 一般病棟の医師の総数の基準値を取得します。
		iGeneralWardTotalDoctorNum = (int)GetInitDataInt( strGeneralWardSectionName, "GeneralWardTotalDoctor", -1, strIniFullPath );
		if( 0 > iGeneralWardTotalDoctorNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total doctor in the general ward number is out of range"));
		}
		// 一般病棟の看護師の総数の基準値を取得します。
		iGeneralWardTotalNurseNum = (int)GetInitDataInt( strGeneralWardSectionName, "GeneralWardTotalNurse", -1, strIniFullPath );
		if( 0 > iGeneralWardTotalNurseNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total nurse in the general ward number is out of range"));
		}
		// 一般病棟の看護師数の最小範囲を取得します。
		iGeneralWardNurseNumMinWeight = GetInitDataFloat( strGeneralWardSectionName, "StandardGeneralWardNurseMinWeight", -1, strIniFullPath );
		if( 0 > iGeneralWardNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of nurse in the general ward number is out of range"));
		}
		// 一般病棟の看護師数の最大範囲を取得します。
		iGeneralWardNurseNumMaxWeight = GetInitDataFloat( strGeneralWardSectionName, "StandardGeneralWardNurseMaxWeight", -1, strIniFullPath );
		if( 0 > iGeneralWardNurseNumMaxWeight || iGeneralWardNurseNumMaxWeight < iGeneralWardNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of nurse in the general ward is out of range"));
		}
		// 一般病棟の医師の最小範囲を取得します。
		iGeneralWardDoctorNumMinWeight = GetInitDataFloat( strGeneralWardSectionName, "StandardGeneralWardDoctorMinWeight", -1, strIniFullPath );
		if( 0 > iGeneralWardDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of doctor in the general ward number is out of range"));
		}
		// 一般病棟の医師の最大範囲を取得します。
		iGeneralWardDoctorNumMaxWeight = GetInitDataFloat( strGeneralWardSectionName, "StandardGeneralWardDoctorMaxWeight", -1, strIniFullPath );
		if( 0 > iGeneralWardDoctorNumMaxWeight || iGeneralWardDoctorNumMaxWeight < iGeneralWardDoctorNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of doctor in the general ward is out of range"));
		}

		// 待合室数の最小値を取得します。
		iWaitingRoomNumMin = (int)GetInitDataInt( strWaitingRoomSectionName, "WaitingRoomMin", -1, strIniFullPath );
		if( 0 > iWaitingRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of waiting room number is out of range"));
		}
		// 待合室数の最大値を取得します。
		iWaitingRoomNumMax = (int)GetInitDataInt( strWaitingRoomSectionName, "WaitingRoomMax", -1, strIniFullPath );
		if( 0 > iWaitingRoomNumMin || iWaitingRoomNumMax < iWaitingRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of waiting room number is out of range"));
		}
		// 1待合室の看護師の最小値を取得します。
		iWaitingRoomNurseNumMin = (int)GetInitDataInt( strWaitingRoomSectionName, "WaitingRoomNurseMin", -1, strIniFullPath );
		if( 0 > iWaitingRoomNurseNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of nurse in the waiting room number is out of range"));
		}
		// 1待合室の看護師の最大値を取得します。
		iWaitingRoomNurseNumMax = (int)GetInitDataInt( strWaitingRoomSectionName, "WaitingRoomNurseMax", -1, strIniFullPath );
		if( 0 > iWaitingRoomNurseNumMax || iWaitingRoomNurseNumMax < iWaitingRoomNurseNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of nurse in the waiting room is out of range"));
		}
		// 待合室の看護師の総数の基準値を取得します。
		iWaitingRoomTotalNurseNum = (int)GetInitDataInt( strWaitingRoomSectionName, "WaitingRoomTotalNurse", -1, strIniFullPath );
		if( 0 > iWaitingRoomTotalNurseNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total nurse in the waiting room number is out of range"));
		}
		// 待合室の看護師数の最小範囲を取得します。
		iWaitingRoomNurseNumMinWeight = GetInitDataFloat( strWaitingRoomSectionName, "StandardWaitingRoomNurseMinWeight", -1, strIniFullPath );
		if( 0 > iWaitingRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of nurse in the waiting room number is out of range"));
		}
		// 待合室の看護師数の最大範囲を取得します。
		iWaitingRoomNurseNumMaxWeight = GetInitDataFloat( strWaitingRoomSectionName, "StandardWaitingRoomNurseMaxWeight", -1, strIniFullPath );
		if( 0 > iWaitingRoomNurseNumMaxWeight || iWaitingRoomNurseNumMaxWeight < iWaitingRoomNurseNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of nurse in the waiting room is out of range"));
		}

		// X線室数の最小値を取得します。
		iXRayRoomNumMin = (int)GetInitDataInt( strXRayRoomSectionName, "XRayRoomMin", -1, strIniFullPath );
		if( 0 > iXRayRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of x-ray room number is out of range"));
		}
		// X線室数の最大値を取得します。
		iXRayRoomNumMax = (int)GetInitDataInt( strXRayRoomSectionName, "XRayRoomMax", -1, strIniFullPath );
		if( 0 > iXRayRoomNumMin || iXRayRoomNumMax < iXRayRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of x-ray room number is out of range"));
		}
		// 1X線室の医療技師の最小値を取得します。
		iXRayRoomClinicalEngineerNumMin = (int)GetInitDataInt( strXRayRoomSectionName, "XRayRoomClinicalEngineerMin", -1, strIniFullPath );
		if( 0 > iXRayRoomClinicalEngineerNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of clinical engineer in the x-ray room number is out of range"));
		}
		// 1X線室の医療技師の最大値を取得します。
		iXRayRoomClinicalEngineerNumMax = (int)GetInitDataInt( strXRayRoomSectionName, "XRayRoomClinicalEngineerMax", -1, strIniFullPath );
		if( 0 > iXRayRoomClinicalEngineerNumMax || iXRayRoomClinicalEngineerNumMax < iXRayRoomClinicalEngineerNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of clinical engineer in the x-ray room is out of range"));
		}
		// X線室の医療技師の総数の基準値を取得します。
		iXRayRoomTotalClinicalEngineerNum = (int)GetInitDataInt( strXRayRoomSectionName, "XRayRoomTotalClinicalEngineer", -1, strIniFullPath );
		if( 0 > iXRayRoomTotalClinicalEngineerNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total clinical engineer in the x-ray room number is out of range"));
		}
		// X線室の医療技師数の最小範囲を取得します。
		iXRayRoomClinicalEngineerNumMinWeight = GetInitDataFloat( strXRayRoomSectionName, "StandardXRayRoomClinicalEngineerMinWeight", -1, strIniFullPath );
		if( 0 > iXRayRoomClinicalEngineerNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of clinical engineer in the x-ray room number is out of range"));
		}
		// X線室の医療技師数の最大範囲を取得します。
		iXRayRoomClinicalEngineerNumMaxWeight = GetInitDataFloat( strXRayRoomSectionName, "StandardXRayRoomClinicalEngineerMaxWeight", -1, strIniFullPath );
		if( 0 > iXRayRoomClinicalEngineerNumMaxWeight || iXRayRoomClinicalEngineerNumMaxWeight < iXRayRoomClinicalEngineerNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of clinical engineer in the x-ray room is out of range"));
		}

		// CT室数の最小値を取得します。
		iCTRoomNumMin = (int)GetInitDataInt( strCTRoomSectionName, "CTRoomMin", -1, strIniFullPath );
		if( 0 > iCTRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of ct room number is out of range"));
		}
		// CT室数の最大値を取得します。
		iCTRoomNumMax = (int)GetInitDataInt( strCTRoomSectionName, "CTRoomMax", -1, strIniFullPath );
		if( 0 > iCTRoomNumMin || iCTRoomNumMax < iCTRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of ct room number is out of range"));
		}
		// 1CT室の医療技師の最小値を取得します。
		iCTRoomClinicalEngineerNumMin = (int)GetInitDataInt( strCTRoomSectionName, "CTRoomClinicalEngineerMin", -1, strIniFullPath );
		if( 0 > iCTRoomClinicalEngineerNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of clinical engineer in the ct room number is out of range"));
		}
		// 1CT室の医療技師の最大値を取得します。
		iCTRoomClinicalEngineerNumMax = (int)GetInitDataInt( strCTRoomSectionName, "CTRoomClinicalEngineerMax", -1, strIniFullPath );
		if( 0 > iCTRoomClinicalEngineerNumMax || iCTRoomClinicalEngineerNumMax < iCTRoomClinicalEngineerNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of clinical engineer in the ct room is out of range"));
		}
		// CT室の医療技師の総数の基準値を取得します。
		iCTRoomTotalClinicalEngineerNum = (int)GetInitDataInt( strCTRoomSectionName, "CTRoomTotalClinicalEngineer", -1, strIniFullPath );
		if( 0 > iCTRoomTotalClinicalEngineerNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total clinical engineer in the ct room number is out of range"));
		}
		// CT室の医療技師数の最小範囲を取得します。
		iCTRoomClinicalEngineerNumMinWeight = GetInitDataFloat( strCTRoomSectionName, "StandardCTRoomClinicalEngineerMinWeight", -1, strIniFullPath );
		if( 0 > iCTRoomClinicalEngineerNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of clinical engineer in the ct room number is out of range"));
		}
		// CT室の医療技師数の最大範囲を取得します。
		iCTRoomClinicalEngineerNumMaxWeight = GetInitDataFloat( strCTRoomSectionName, "StandardCTRoomClinicalEngineerMaxWeight", -1, strIniFullPath );
		if( 0 > iCTRoomClinicalEngineerNumMaxWeight || iCTRoomClinicalEngineerNumMaxWeight < iCTRoomClinicalEngineerNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of clinical engineer in the ct room is out of range"));
		}

		// MRI室数の最小値を取得します。
		iMRIRoomNumMin = (int)GetInitDataInt( strMRIRoomSectionName, "MRIRoomMin", -1, strIniFullPath );
		if( 0 > iMRIRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of mri room number is out of range"));
		}
		// MRI室数の最大値を取得します。
		iMRIRoomNumMax = (int)GetInitDataInt( strMRIRoomSectionName, "MRIRoomMax", -1, strIniFullPath );
		if( 0 > iMRIRoomNumMin || iMRIRoomNumMax < iMRIRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of mri room number is out of range"));
		}
		// MRI室の医療技師の総数の最小値を取得します。
		iMRIRoomTotalClinicalEngineerNum = (int)GetInitDataInt( strMRIRoomSectionName, "MRIRoomTotalClinicalEngineer", -1, strIniFullPath );
		if( 0 > iMRIRoomTotalClinicalEngineerNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of total clinical engineer in the mri room number is out of range"));
		}
		// 1MRI室の医療技師の最小値を取得します。
		iMRIRoomClinicalEngineerNumMin = (int)GetInitDataInt( strMRIRoomSectionName, "MRIRoomClinicalEngineerMin", -1, strIniFullPath );
		if( 0 > iMRIRoomClinicalEngineerNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of clinical engineer in the mri room number is out of range"));
		}
		// 1MRI室の医療技師の最大値を取得します。
		iMRIRoomClinicalEngineerNumMax = (int)GetInitDataInt( strMRIRoomSectionName, "MRIRoomClinicalEngineerMax", -1, strIniFullPath );
		if( 0 > iMRIRoomClinicalEngineerNumMax || iMRIRoomClinicalEngineerNumMax < iMRIRoomClinicalEngineerNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of clinical engineer in the mri room is out of range"));
		}
		// MRI室の医療技師数の最小範囲を取得します。
		iMRIRoomClinicalEngineerNumMinWeight = GetInitDataFloat( strMRIRoomSectionName, "StandardMRIRoomClinicalEngineerMinWeight", -1, strIniFullPath );
		if( 0 > iMRIRoomClinicalEngineerNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of clinical engineer in the mri room number is out of range"));
		}
		// MRI室の医療技師数の最大範囲を取得します。
		iMRIRoomClinicalEngineerNumMaxWeight = GetInitDataFloat( strMRIRoomSectionName, "StandardMRIRoomClinicalEngineerMaxWeight", -1, strIniFullPath );
		if( 0 > iMRIRoomClinicalEngineerNumMaxWeight || iMRIRoomClinicalEngineerNumMaxWeight < iMRIRoomClinicalEngineerNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of clinical engineer in the mri room is out of range"));
		}

		// 血管造影室数の最小値を取得します。
		iAngiographyRoomNumMin = (int)GetInitDataInt( strAngiographyRoomSectionName, "AngiographyRoomMin", -1, strIniFullPath );
		if( 0 > iAngiographyRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of angiography room number is out of range"));
		}
		// 血管造影室数の最大値を取得します。
		iAngiographyRoomNumMax = (int)GetInitDataInt( strAngiographyRoomSectionName, "AngiographyRoomMax", -1, strIniFullPath );
		if( 0 > iAngiographyRoomNumMin || iAngiographyRoomNumMax < iAngiographyRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of angiography room number is out of range"));
		}
		// 1血管造影室の医療技師の最小値を取得します。
		iAngiographyRoomClinicalEngineerNumMin = (int)GetInitDataInt( strAngiographyRoomSectionName, "AngiographyRoomClinicalEngineerMin", -1, strIniFullPath );
		if( 0 > iAngiographyRoomClinicalEngineerNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of clinical engineer in the angiography room number is out of range"));
		}
		// 1血管造影室の医療技師の最大値を取得します。
		iAngiographyRoomClinicalEngineerNumMax = (int)GetInitDataInt( strAngiographyRoomSectionName, "AngiographyRoomClinicalEngineerMax", -1, strIniFullPath );
		if( 0 > iAngiographyRoomClinicalEngineerNumMax || iAngiographyRoomClinicalEngineerNumMax < iAngiographyRoomClinicalEngineerNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of clinical engineer in the angiography room is out of range"));
		}
		// 血管造影室医療技師の総数の基準値を取得します。
		iAngiographyRoomTotalClinicalEngineerNum = (int)GetInitDataInt( strAngiographyRoomSectionName, "AngiographyRoomTotalClinicalEngineer", -1, strIniFullPath );
		if( 0 > iAngiographyRoomTotalClinicalEngineerNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total clinical engineer in the angiography room number is out of range"));
		}
		// 血管造影室の医療技師数の最小範囲を取得します。
		iAngiographyRoomClinicalEngineerNumMinWeight = GetInitDataFloat( strAngiographyRoomSectionName, "StandardAngiographyRoomClinicalEngineerMinWeight", -1, strIniFullPath );
		if( 0 > iAngiographyRoomClinicalEngineerNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of clinical engineer in the angiography room number is out of range"));
		}
		// 血管造影室の医療技師数の最大範囲を取得します。
		iAngiographyRoomClinicalEngineerNumMaxWeight = GetInitDataFloat( strAngiographyRoomSectionName, "StandardAngiographyRoomClinicalEngineerMaxWeight", -1, strIniFullPath );
		if( 0 > iAngiographyRoomClinicalEngineerNumMaxWeight || iAngiographyRoomClinicalEngineerNumMaxWeight < iAngiographyRoomClinicalEngineerNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("angiographyf clinical engineer in the angiography room is out of range"));
		}

		// FAST数の最小値を取得します。
		iFastRoomNumMin = (int)GetInitDataInt( strFastRoomSectionName, "FastRoomMin", -1, strIniFullPath );
		if( 0 > iFastRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of fast room number is out of range"));
		}
		// FAST室最大値を取得します。
		iFastRoomNumMax = (int)GetInitDataInt( strFastRoomSectionName, "FastRoomMax", -1, strIniFullPath );
		if( 0 > iFastRoomNumMin || iFastRoomNumMax < iFastRoomNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of fast room number is out of range"));
		}
		// 1FAST室の医療技師の最小値を取得します。
		iFastRoomClinicalEngineerNumMin = (int)GetInitDataInt( strFastRoomSectionName, "FastRoomClinicalEngineerMin", -1, strIniFullPath );
		if( 0 > iFastRoomClinicalEngineerNumMin )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min value of clinical engineer in the fast room number is out of range"));
		}
		// 1FAST室の医療技師の最大値を取得します。
		iFastRoomClinicalEngineerNumMax = (int)GetInitDataInt( strFastRoomSectionName, "FastRoomClinicalEngineerMax", -1, strIniFullPath );
		if( 0 > iFastRoomClinicalEngineerNumMax || iFastRoomClinicalEngineerNumMax < iFastRoomClinicalEngineerNumMin)
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max value of clinical engineer in the fast room is out of range"));
		}
		// FAST室の医療技師の総数の基準値を取得します。
		iFastRoomTotalClinicalEngineerNum = (int)GetInitDataInt( strFastRoomSectionName, "FastRoomTotalClinicalEngineer", -1, strIniFullPath );
		if( 0 > iFastRoomTotalClinicalEngineerNum )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("value of total clinical engineer in the fast room number is out of range"));
		}
		// 1FAST室の医療技師数の最小範囲を取得します。
		iFastRoomClinicalEngineerNumMinWeight = GetInitDataFloat( strFastRoomSectionName, "StandardFastRoomClinicalEngineerMinWeight", -1, strIniFullPath );
		if( 0 > iFastRoomClinicalEngineerNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("min weight of clinical engineer in the fast room number is out of range"));
		}
		// 1FAST室の医療技師数の最大範囲を取得します。
		iFastRoomClinicalEngineerNumMaxWeight = GetInitDataFloat( strFastRoomSectionName, "StandardFastRoomClinicalEngineerMaxWeight", -1, strIniFullPath );
		if( 0 > iFastRoomClinicalEngineerNumMaxWeight || iFastRoomClinicalEngineerNumMaxWeight < iFastRoomClinicalEngineerNumMinWeight )
		{
			/* 範囲外を指定した場合はエラーを出力します。*/
			throw(new IllegalArgumentException("max weight of clinical engineer in the fast room is out of range"));
		}
	}

	public int iGetConsultationRoomNumMax()
	{
		return iConsultationRoomNumMax;
	}
	public int iGetConsultationRoomNumMin()
	{
		return iConsultationRoomNumMin;
	}
	public int iGetConsultationRoomDoctorNumMax()
	{
		return iConsultationRoomDoctorNumMax;
	}
	public int iGetConsultationRoomDoctorNumMin()
	{
		return iConsultationRoomDoctorNumMin;
	}
	public int iGetConsultationRoomNurseNumMax()
	{
		return iConsultationRoomNurseNumMax;
	}
	public int iGetConsultationRoomNurseNumMin()
	{
		return iConsultationRoomNurseNumMin;
	}
	public int iGetConsultationRoomTotalDoctorNum()
	{
		return iConsultationRoomTotalDoctorNum;
	}
	public int iGetConsultationRoomTotalNurseNum()
	{
		return iConsultationRoomTotalNurseNum;
	}
	public double lfGetConsultationRoomDoctorNumMinWeight()
	{
		return iConsultationRoomDoctorNumMinWeight;
	}
	public double lfGetConsultationRoomDoctorNumMaxWeight()
	{
		return iConsultationRoomDoctorNumMaxWeight;
	}
	public double lfGetConsultationRoomNurseNumMinWeight()
	{
		return iConsultationRoomNurseNumMinWeight;
	}
	public double lfGetConsultationRoomNurseNumMaxWeight()
	{
		return iConsultationRoomNurseNumMaxWeight;
	}

	public int iGetOperationRoomNumMax()
	{
		return iOperationRoomNumMax;
	}
	public int iGetOperationRoomNumMin()
	{
		return iOperationRoomNumMin;
	}
	public int iGetOperationRoomDoctorNumMax()
	{
		return iOperationRoomDoctorNumMax;
	}
	public int iGetOperationRoomDoctorNumMin()
	{
		return iOperationRoomDoctorNumMin;
	}
	public int iGetOperationRoomNurseNumMax()
	{
		return iOperationRoomNurseNumMax;
	}
	public int iGetOperationRoomNurseNumMin()
	{
		return iOperationRoomNurseNumMin;
	}
	public int iGetOperationRoomTotalDoctorNum()
	{
		return iOperationRoomTotalDoctorNum;
	}
	public int iGetOperationRoomTotalNurseNum()
	{
		return iOperationRoomTotalNurseNum;
	}
	public double lfGetOperationRoomDoctorNumMinWeight()
	{
		return iOperationRoomDoctorNumMinWeight;
	}
	public double lfGetOperationRoomDoctorNumMaxWeight()
	{
		return iOperationRoomDoctorNumMaxWeight;
	}
	public double lfGetOperationRoomNurseNumMinWeight()
	{
		return iOperationRoomNurseNumMinWeight;
	}
	public double lfGetOperationRoomNurseNumMaxWeight()
	{
		return iOperationRoomNurseNumMaxWeight;
	}

	public int iGetEmergencyRoomNumMax()
	{
		 return iEmergencyRoomNumMax;
	}
	public int iGetEmergencyRoomNumMin()
	{
		 return iEmergencyRoomNumMin;
	}
	public int iGetEmergencyRoomDoctorNumMax()
	{
		 return iEmergencyRoomDoctorNumMax;
	}
	public int iGetEmergencyRoomDoctorNumMin()
	{
		 return iEmergencyRoomDoctorNumMin;
	}
	public int iGetEmergencyRoomNurseNumMax()
	{
		 return iEmergencyRoomNurseNumMax;
	}
	public int iGetEmergencyRoomNurseNumMin()
	{
		 return iEmergencyRoomNurseNumMin;
	}
	public int iGetEmergencyRoomClinicalEngineerNumMax()
	{
		 return iEmergencyRoomClinicalEngineerNumMax;
	}
	public int iGetEmergencyRoomClinicalEngineerNumMin()
	{
		 return iEmergencyRoomClinicalEngineerNumMin;
	}
	public int iGetEmergencyRoomTotalDoctorNum()
	{
		return iEmergencyRoomTotalDoctorNum;
	}
	public int iGetEmergencyRoomTotalNurseNum()
	{
		return iEmergencyRoomTotalNurseNum;
	}
	public double lfGetEmergencyRoomDoctorNumMinWeight()
	{
		return iEmergencyRoomDoctorNumMinWeight;
	}
	public double lfGetEmergencyRoomDoctorNumMaxWeight()
	{
		return iEmergencyRoomDoctorNumMaxWeight;
	}
	public double lfGetEmergencyRoomNurseNumMinWeight()
	{
		return iEmergencyRoomNurseNumMinWeight;
	}
	public double lfGetEmergencyRoomNurseNumMaxWeight()
	{
		return iEmergencyRoomNurseNumMaxWeight;
	}

	public int iGetObservationRoomNumMax()
	{
		 return iObservationRoomNumMax;
	}
	public int iGetObservationRoomNumMin()
	{
		 return iObservationRoomNumMin;
	}
	public int iGetObservationRoomNurseNumMax()
	{
		 return iObservationRoomNurseNumMax;
	}
	public int iGetObservationRoomNurseNumMin()
	{
		return iObservationRoomNurseNumMin;
	}
	public int iGetObservationRoomTotalNurseNum()
	{
		return iObservationRoomTotalNurseNum;
	}
	public double lfGetObservationRoomNurseNumMinWeight()
	{
		return iObservationRoomNurseNumMinWeight;
	}
	public double lfGetObservationRoomNurseNumMaxWeight()
	{
		return iObservationRoomNurseNumMaxWeight;
	}

	public int iGetSevereInjuryObservationRoomNumMax()
	{
		 return iSevereInjuryObservationRoomNumMax;
	}
	public int iGetSevereInjuryObservationRoomNumMin()
	{
		 return iSevereInjuryObservationRoomNumMin;
	}
	public int iGetSevereInjuryObservationRoomNurseNumMax()
	{
		 return iSevereInjuryObservationRoomNurseNumMax;
	}
	public int iGetSevereInjuryObservationRoomNurseNumMin()
	{
		 return iSevereInjuryObservationRoomNurseNumMin;
	}
	public int iGetSevereInjuryObservationRoomTotalNurseNum()
	{
		return iSevereInjuryObservationRoomTotalNurseNum;
	}
	public double lfGetSevereInjuryObservationRoomNurseNumMinWeight()
	{
		return iSevereInjuryObservationRoomNurseNumMinWeight;
	}
	public double lfGetSevereInjuryObservationRoomNurseNumMaxWeight()
	{
		return iSevereInjuryObservationRoomNurseNumMaxWeight;
	}

	public int iGetIntensiveCareUnitNumMax()
	{
		 return iIntensiveCareUnitNumMax;
	}
	public int iGetIntensiveCareUnitNumMin()
	{
		 return iIntensiveCareUnitNumMin;
	}
	public int iGetIntensiveCareUnitDoctorNumMax()
	{
		 return iIntensiveCareUnitDoctorNumMax;
	}
	public int iGetIntensiveCareUnitDoctorNumMin()
	{
		 return iIntensiveCareUnitDoctorNumMin;
	}
	public int iGetIntensiveCareUnitNurseNumMax()
	{
		 return iIntensiveCareUnitNurseNumMax;
	}
	public int iGetIntensiveCareUnitNurseNumMin()
	{
		 return iIntensiveCareUnitNurseNumMin;
	}
	public int iGetIntensiveCareUnitTotalDoctorNum()
	{
		return iIntensiveCareUnitTotalDoctorNum;
	}
	public int iGetIntensiveCareUnitTotalNurseNum()
	{
		return iIntensiveCareUnitTotalNurseNum;
	}
	public double lfGetIntensiveCareUnitDoctorNumMinWeight()
	{
		return iIntensiveCareUnitDoctorNumMinWeight;
	}
	public double lfGetIntensiveCareUnitDoctorNumMaxWeight()
	{
		return iIntensiveCareUnitDoctorNumMaxWeight;
	}
	public double lfGetIntensiveCareUnitNurseNumMinWeight()
	{
		return iIntensiveCareUnitNurseNumMinWeight;
	}
	public double lfGetIntensiveCareUnitNurseNumMaxWeight()
	{
		return iIntensiveCareUnitNurseNumMaxWeight;
	}

	public int iGetHighCareUnitNumMax()
	{
		 return iHighCareUnitNumMax;
	}
	public int iGetHighCareUnitNumMin()
	{
		 return iHighCareUnitNumMin;
	}
	public int iGetHighCareUnitDoctorNumMax()
	{
		 return iHighCareUnitDoctorNumMax;
	}
	public int iGetHighCareUnitDoctorNumMin()
	{
		 return iHighCareUnitDoctorNumMin;
	}
	public int iGetHighCareUnitNurseNumMax()
	{
		 return iHighCareUnitNurseNumMax;
	}
	public int iGetHighCareUnitNurseNumMin()
	{
		 return iHighCareUnitNurseNumMin;
	}
	public int iGetHighCareUnitTotalDoctorNum()
	{
		return iHighCareUnitTotalDoctorNum;
	}
	public int iGetHighCareUnitTotalNurseNum()
	{
		return iHighCareUnitTotalNurseNum;
	}
	public double lfGetHighCareUnitDoctorNumMinWeight()
	{
		return iHighCareUnitDoctorNumMinWeight;
	}
	public double lfGetHighCareUnitDoctorNumMaxWeight()
	{
		return iHighCareUnitDoctorNumMaxWeight;
	}
	public double lfGetHighCareUnitNurseNumMinWeight()
	{
		return iHighCareUnitNurseNumMinWeight;
	}
	public double lfGetHighCareUnitNurseNumMaxWeight()
	{
		return iHighCareUnitNurseNumMaxWeight;
	}

	public int iGetGeneralWardNumMax()
	{
		 return iGeneralWardNumMax;
	}
	public int iGetGeneralWardNumMin()
	{
		 return iGeneralWardNumMin;
	}
	public int iGetGeneralWardDoctorNumMax()
	{
		 return iGeneralWardDoctorNumMax;
	}
	public int iGetGeneralWardDoctorNumMin()
	{
		 return iGeneralWardDoctorNumMin;
	}
	public int iGetGeneralWardNurseNumMax()
	{
		 return iGeneralWardNurseNumMax;
	}
	public int iGetGeneralWardNurseNumMin()
	{
		 return iGeneralWardNurseNumMin;
	}
	public int iGetGeneralWardTotalDoctorNum()
	{
		return iGeneralWardTotalDoctorNum;
	}
	public int iGetGeneralWardTotalNurseNum()
	{
		return iGeneralWardTotalNurseNum;
	}
	public double lfGetGeneralWardDoctorNumMinWeight()
	{
		return iGeneralWardDoctorNumMinWeight;
	}
	public double lfGetGeneralWardDoctorNumMaxWeight()
	{
		return iGeneralWardDoctorNumMaxWeight;
	}
	public double lfGetGeneralWardNurseNumMinWeight()
	{
		return iGeneralWardNurseNumMinWeight;
	}
	public double lfGetGeneralWardNurseNumMaxWeight()
	{
		return iGeneralWardNurseNumMaxWeight;
	}

	public int iGetWaitingRoomNumMax()
	{
		 return iWaitingRoomNumMax;
	}
	public int iGetWaitingRoomNumMin()
	{
		 return iWaitingRoomNumMin;
	}
	public int iGetWaitingRoomNurseNumMax()
	{
		 return iWaitingRoomNurseNumMax;
	}
	public int iGetWaitingRoomNurseNumMin()
	{
		 return iWaitingRoomNurseNumMin;
	}
	public int iGetWaitingRoomTotalNurseNum()
	{
		return iWaitingRoomTotalNurseNum;
	}
	public double lfGetWaitingRoomNurseNumMinWeight()
	{
		return iWaitingRoomNurseNumMinWeight;
	}
	public double lfGetWaitingRoomNurseNumMaxWeight()
	{
		return iWaitingRoomNurseNumMaxWeight;
	}

	public int iGetXRayRoomNumMax()
	{
		 return iXRayRoomNumMax;
	}
	public int iGetXRayRoomNumMin()
	{
		 return iXRayRoomNumMin;
	}
	public int iGetXRayRoomClinicalEngineerNumMax()
	{
		 return iXRayRoomClinicalEngineerNumMax;
	}
	public int iGetXRayRoomClinicalEngineerNumMin()
	{
		 return iXRayRoomClinicalEngineerNumMin;
	}
	public int iGetXRayRoomTotalClinicalEngineerNum()
	{
		return iXRayRoomTotalClinicalEngineerNum;
	}
	public double lfGetXRayRoomClinicalEngineerNumMinWeight()
	{
		return iXRayRoomClinicalEngineerNumMinWeight;
	}
	public double lfGetXRayRoomClinicalEngineerNumMaxWeight()
	{
		return iXRayRoomClinicalEngineerNumMaxWeight;
	}

	public int iGetCTRoomNumMax()
	{
		 return iCTRoomNumMax;
	}
	public int iGetCTRoomNumMin()
	{
		 return iCTRoomNumMin;
	}
	public int iGetCTRoomClinicalEngineerNumMax()
	{
		 return iCTRoomClinicalEngineerNumMax;
	}
	public int iGetCTRoomClinicalEngineerNumMin()
	{
		 return iCTRoomClinicalEngineerNumMin;
	}
	public int iGetCTRoomTotalClinicalEngineerNum()
	{
		return iCTRoomTotalClinicalEngineerNum;
	}
	public double lfGetCTRoomClinicalEngineerNumMinWeight()
	{
		return iCTRoomClinicalEngineerNumMinWeight;
	}
	public double lfGetCTRoomClinicalEngineerNumMaxWeight()
	{
		return iCTRoomClinicalEngineerNumMaxWeight;
	}

	public int iGetMRIRoomNumMax()
	{
		return iMRIRoomNumMax;
	}
	public int iGetMRIRoomNumMin()
	{
		return iMRIRoomNumMin;
	}
	public int iGetMRIRoomClinicalEngineerNumMax()
	{
		return iMRIRoomClinicalEngineerNumMax;
	}
	public int iGetMRIRoomClinicalEngineerNumMin()
	{
		return iMRIRoomClinicalEngineerNumMin;
	}
	public int iGetMRIRoomTotalClinicalEngineerNum()
	{
		return iMRIRoomTotalClinicalEngineerNum;
	}
	public double lfGetMRIRoomClinicalEngineerNumMinWeight()
	{
		return iMRIRoomClinicalEngineerNumMinWeight;
	}
	public double lfGetMRIRoomClinicalEngineerNumMaxWeight()
	{
		return iMRIRoomClinicalEngineerNumMaxWeight;
	}

	public int iGetAngiographyRoomNumMax()
	{
		return iAngiographyRoomNumMax;
	}
	public int iGetAngiographyRoomNumMin()
	{
		return iAngiographyRoomNumMin;
	}
	public int iGetAngiographyRoomClinicalEngineerNumMax()
	{
		return iAngiographyRoomClinicalEngineerNumMax;
	}
	public int iGetAngiographyRoomClinicalEngineerNumMin()
	{
		return iAngiographyRoomClinicalEngineerNumMin;
	}
	public int iGetAngiographyRoomTotalClinicalEngineerNum()
	{
		return iAngiographyRoomTotalClinicalEngineerNum;
	}
	public double lfGetAngiographyRoomClinicalEngineerNumMinWeight()
	{
		return iAngiographyRoomClinicalEngineerNumMinWeight;
	}
	public double lfGetAngiographyRoomClinicalEngineerNumMaxWeight()
	{
		return iAngiographyRoomClinicalEngineerNumMaxWeight;
	}

	public int iGetFastRoomNumMax()
	{
		return iFastRoomNumMax;
	}
	public int iGetFastRoomNumMin()
	{
		return iFastRoomNumMin;
	}
	public int iGetFastRoomClinicalEngineerNumMax()
	{
		return iFastRoomClinicalEngineerNumMax;
	}
	public int iGetFastRoomClinicalEngineerNumMin()
	{
		return iFastRoomClinicalEngineerNumMin;
	}
	public int iGetFastRoomTotalClinicalEngineerNum()
	{
		return iFastRoomTotalClinicalEngineerNum;
	}
	public double lfGetFastRoomClinicalEngineerNumMinWeight()
	{
		return iFastRoomClinicalEngineerNumMinWeight;
	}
	public double lfGetFastRoomClinicalEngineerNumMaxWeight()
	{
		return iFastRoomClinicalEngineerNumMaxWeight;
	}

}
