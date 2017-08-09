package inverse.optimization.constraintcondition;

import utility.initparam.InitInverseSimParam;

public class ConstraintCondition implements ConstraintConditionInterface
{
	int iMode;						// 使用する制約条件を表す変数。
	InitInverseSimParam invParam;	// 逆シミュレーション用調整パラメータ

	/**
	 * <PRE>
	 *   制約条件設定のコンストラクタ
	 * </PRE>
	 */
	public ConstraintCondition()
	{
		iMode = 1;
	}

	/**
	 * <PRE>
	 *   制約条件設定のコンストラクタ
	 * </PRE>
	 * @param iMode			制約条件
	 * @param invParam		逆シミュレーション用調整パラメータ用インスタンス
	 */
	public ConstraintCondition(int iMode, InitInverseSimParam invParam)
	{
		this.iMode = iMode;
		this.invParam = invParam;
	}

	@Override
	public void vConstraintCondition( double[] plfArg )
	{
//		iMode = 1;
		// TODO 自動生成されたメソッド・スタブ
		if( iMode == 1 )
		{
			vSetERCondition( plfArg );
		}
		else if( iMode == 2 )
		{
			vSetEDCondition( plfArg );
		}
		else if( iMode == 3 )
		{
			vSetEDCondition2( plfArg );
		}
		else if( iMode == 4 )
		{
			vSetEDCalibrationCondition( plfArg );
		}
		else
		{
			// 特に制約条件はなしとします。
		}
	}

	@Override
	public void vSetConditionMode( int iMode )
	{
		// TODO 自動生成されたメソッド・スタブ
		this.iMode = iMode;
	}

	/**
	 * <PRE>
	 *   救急部門用の一般的な制約条件設定
	 * </PRE>
	 * @param plfArg	制約条件算出用引数
	 */
	private void vSetERCondition( double[] plfArg )
	{
		int i;
		for( i = 0;i < plfArg.length; i++ )
		{
		// 部屋の設定

			// 観察室、重症観察室以外は部屋がない場合が考えられないので、1以上を設定します。
			if( i != 3 && i != 4 )
			{
				if( plfArg[i] <= 1.0 )
					plfArg[i] = 1.0;
			}
			// 観察室、重症観察室は部屋を用意していない病院があるので、0以上とします。
			else
			{
				if( plfArg[i] <= 0.0 )	plfArg[i] = 0.0;
			}
			// 診察室、手術室、初療室は20部屋以下とします。
			if( i == 0 || i == 1 || i == 2 )
			{
				if( plfArg[i] > 20.0 ) plfArg[i] = 20.0;
			}
			// ICU、HCUは30部屋以下とします。
			if( i == 5 || i == 6 )
			{
				if( plfArg[i] > 30.0 ) plfArg[i] = 30.0;
			}

			// 一般病棟は制限なしとします。
			if( i == 7 )
			{

			}

			// 各検査室（X線室、CT室、MRI室、血管造影室、Fast室）は10部屋以下とします。
			if( i == 9 || i == 10 || i == 11 || i == 12 || i == 13 )
			{
				if( plfArg[i] > 10.0 ) plfArg[i] = 10.0;
			}

		// 部屋を構成するエージェント数の設定

			// 各診察室の医師数は最大でも2人くらいに設定します。
			if( i == 14 )
			{
				if( plfArg[i] > 2.0 ) plfArg[i] = 2.0;
			}
			// 各診察室の看護師数は最大4人に設定します。
			if( i == 15 )
			{
				if( plfArg[i] > 4.0 ) plfArg[i] = 4.0;
			}
			// 各手術室及び初療室の医師数は最大でも3人くらいと設定します。
			if( i == 16 || i == 18 )
			{
				if( plfArg[i] > 3.0 ) plfArg[i] = 3.0;
			}
			// 各手術室、各初療室の看護師数は最大で10人くらいと設定します。
			if( i == 17 || i == 19 )
			{
				if( plfArg[i] > 10.0 ) plfArg[i] = 10.0;
			}

			if( i == 20 )
			{

			}
			// 各観察室、重症観察室に所属する看護師は2人以下とします。
			if( i == 21 || i == 22 )
			{
				if( plfArg[i] > 2.0 ) plfArg[i] = 2.0;
			}

			// 各ICU、HCU、一般病棟の医師数を2人以下とします。
			if( i == 23 || i == 25 || i == 27 )
			{
				if( plfArg[i] > 2.0 ) plfArg[i] = 2.0;
			}
			// 各ICU、HCU、一般病棟の看護師数を7人以下とします。
			if( i == 24 || i == 26 || i == 28 || i == 29 )
			{
				if( plfArg[i] > 7.0 ) plfArg[i] = 7.0;
			}

			// 各検査室に所属する医療技師数は5人以下とします。
			if( i == 30 || i == 31 || i == 32 || i == 33 || i == 34 )
			{
				if( plfArg[i] > 5.0 ) plfArg[i] = 5.0;
			}
		}
	}

	/**
	 * <PRE>
	 *   救急部門用の一般的な制約条件設定
	 * </PRE>
	 * @param plfArg	制約条件用引数
	 */
	private void vSetEDCondition( double[] plfArg )
	{
//		double lfPatientsNum;
//		double lfDoctorsNum;
//		double lfNursesNum;
//		double lfClinicalEngineersNum;
//		double lfTotalRoomNum;
//		double lfOneRoomDoctorNum;
//		double lfOneRoomNurseNum;
//		double lfOneRoomClinicalEngineerNum;
//		int i;

	// 部屋の設定

	// 診察室の制約条件を設定します。
		for(;;)
		{
			// 診察室の医師数
			if( plfArg[0]*plfArg[14] <= 42 )
			{
				// 診察室の部屋数を設定します。
				// 調査結果を基に設定。
				if( plfArg[0] > 20.0 ) plfArg[0] = 20.0;
				if( plfArg[0] < 4.0 ) plfArg[0] = 4.0;
				// 診察室の医師数を設定します。
				// 現実的に考えて1部屋に1人が担当医だと考えられるので。2人は多く見積もった。
				if( plfArg[14] < 1.0 ) plfArg[14] = 1.0;
				if( plfArg[14] > 2.0 ) plfArg[14] = 2.0;
				break;
			}
			else
			{
				if( plfArg[0] > 20.0 ) plfArg[0] = 20.0;
				plfArg[0] -= 1.0;
				if( plfArg[0] < 4.0 ) plfArg[0] = 4.0;
				if( plfArg[0]*plfArg[14] <= 42 )
				{
					if( plfArg[14] < 1.0 ) plfArg[14] = 1.0;
					if( plfArg[14] > 2.0 ) plfArg[14] = 2.0;
				}
				else
				{
					if( plfArg[14] > 2.0 ) plfArg[14] = 2.0;
					plfArg[14] -= 1.0;
					if( plfArg[14] < 1.0 ) plfArg[14] = 1.0;
				}
			}
		}
		for(;;)
		{
			// 診察室の看護師数
			if( plfArg[0]*plfArg[15] <= 42 )
			{
				// 診察室の部屋数を設定します。
				// 調査結果を基に設定。
				if( plfArg[0] > 20.0 ) plfArg[0] = 20.0;
				if( plfArg[0] < 4.0 ) plfArg[0] = 4.0;
				// 各診察室の看護師数は最大2人に設定します。
				// 現実的に考えてサポートで1人いると思われるが、多く見積もって2人とした。
				if( plfArg[15] < 0.0 ) plfArg[15] = 0.0;
				if( plfArg[15] > 2.0 ) plfArg[15] = 2.0;
				break;
			}
			else
			{
				if( plfArg[0] > 20.0 ) plfArg[0] = 20.0;
				plfArg[0] -= 1.0;
				if( plfArg[0] < 4.0 ) plfArg[0] = 4.0;
				if( plfArg[0]*plfArg[15] <= 42 )
				{
					if( plfArg[15] < 0.0 ) plfArg[15] = 0.0;
					if( plfArg[15] > 2.0 ) plfArg[15] = 2.0;
				}
				else
				{
					if( plfArg[15] > 2.0 ) plfArg[15] = 2.0;
					plfArg[15] -= 1.0;
					if( plfArg[15] < 0.0 ) plfArg[15] = 0.0;
				}
			}
		}
	// 手術室の制約条件を設定します。
		for(;;)
		{
			// 手術室の医師数
			if( plfArg[1]*plfArg[16] <= 15 )
			{
				// 手術室の部屋数を設定します。
				// 調査結果を基に設定。
				if( plfArg[1] > 15.0 ) plfArg[1] = 15.0;
				if( plfArg[1] < 4.0 ) plfArg[1] = 4.0;
				// 各手術室及び初療室の医師数は最大でも3人くらいと設定します。
				if( plfArg[16] < 1.0 ) plfArg[16] = 1.0;
				if( plfArg[16] > 3.0 ) plfArg[16] = 3.0;
				break;
			}
			else
			{
				if( plfArg[1] > 15.0 ) plfArg[1] = 15.0;
				plfArg[1] -= 1.0;
				if( plfArg[1] < 4.0 ) plfArg[1] = 4.0;
				if( plfArg[1]*plfArg[16] <= 15 )
				{
					if( plfArg[16] < 1.0 ) plfArg[16] = 1.0;
					if( plfArg[16] > 3.0 ) plfArg[16] = 3.0;
				}
				else
				{
					if( plfArg[16] > 3.0 ) plfArg[16] = 3.0;
					plfArg[16] -= 1.0;
					if( plfArg[16] < 1.0 ) plfArg[16] = 1.0;
				}
			}
		}
		// 手術室の看護師数
		for(;;)
		{
			if( plfArg[1]*plfArg[17] <= 82 )
			{
				// 手術室の部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[1] > 23.0 ) plfArg[1] = 23.0;
				if( plfArg[1] < 4.0 ) plfArg[1] = 4.0;
				// 各手術室、各初療室の看護師数は最大で4人くらいと設定します。
				// 公刊文献を調べて3例ほどから。手術室が1部屋当りの人数を算出することができたのでそれを参考に設定。
				if( plfArg[17] < 1.0 ) plfArg[17] = 1.0;
				if( plfArg[17] > 3.0 ) plfArg[17] = 3.0;
				break;
			}
			else
			{
				if( plfArg[1] > 23.0 ) plfArg[1] = 23.0;
				plfArg[1] -= 1.0;
				if( plfArg[1] < 4.0 ) plfArg[1] = 4.0;
				if( plfArg[1]*plfArg[17] <= 82 )
				{
					if( plfArg[17] < 1.0 ) plfArg[17] = 1.0;
					if( plfArg[17] > 3.0 ) plfArg[17] = 3.0;
				}
				else
				{
					if( plfArg[17] > 3.0 ) plfArg[17] = 3.0;
					plfArg[17] -= 1.0;
					if( plfArg[17] < 1.0 ) plfArg[17] = 1.0;
				}
			}
		}
	// 初療室の制約条件を設定します。
		for(;;)
		{
			// 初療室の医師数
			if( plfArg[2]*plfArg[18] <= 42 )
			{
				// 初療室の部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[2] > 14.0 ) plfArg[2] = 14.0;
				if( plfArg[2] < 2.0 ) plfArg[2] = 2.0;
				// 各手術室及び初療室の医師数は最大でも3人くらいと設定します。
				if( plfArg[18] < 1.0 ) plfArg[18] = 1.0;
				if( plfArg[18] > 3.0 ) plfArg[18] = 3.0;
				break;
			}
			else
			{
				if( plfArg[2] > 14.0 ) plfArg[2] = 14.0;
				plfArg[2] -= 1.0;
				if( plfArg[2] < 2.0 ) plfArg[2] = 2.0;
				if( plfArg[2]*plfArg[18] <= 42 )
				{
					if( plfArg[18] < 1.0 ) plfArg[18] = 1.0;
					if( plfArg[18] > 3.0 ) plfArg[18] = 3.0;
				}
				else
				{
					if( plfArg[18] > 3.0 ) plfArg[18] = 3.0;
					plfArg[18] -= 1.0;
					if( plfArg[18] < 1.0 ) plfArg[18] = 1.0;
				}
			}
		}
		for(;;)
		{
			// 初療室の看護師数
			if( plfArg[2]*plfArg[19] <= 31 )
			{
				// 初療室の部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[2] > 14.0 ) plfArg[2] = 14.0;
				if( plfArg[2] < 2.0 ) plfArg[2] = 2.0;
				// 各手術室、各初療室の看護師数は最大で4人くらいと設定します。
				// 公刊文献を調べて3例ほどから。手術室が1部屋当りの人数を算出することができたのでそれを参考に設定。
				if( plfArg[19] < 1.0 ) plfArg[19] = 1.0;
				if( plfArg[19] > 4.0 ) plfArg[19] = 4.0;
				break;
			}
			else
			{
				if( plfArg[2] > 14.0 ) plfArg[2] = 14.0;
				plfArg[2] -= 1.0;
				if( plfArg[2] < 2.0 ) plfArg[2] = 2.0;
				if( plfArg[2]*plfArg[19] <= 31 )
				{
					if( plfArg[19] < 1.0 ) plfArg[19] = 1.0;
					if( plfArg[19] > 4.0 ) plfArg[19] = 4.0;
				}
				else
				{
					if( plfArg[19] > 4.0 ) plfArg[19] = 4.0;
					plfArg[19] -= 1.0;
					if( plfArg[19] < 1.0 ) plfArg[19] = 1.0;
				}
			}
		}
		// 初療室の医療技師数
		if( plfArg[20] < 1.0 ) plfArg[20] = 1.0;
		if( plfArg[20] > 3.0 ) plfArg[20] = 3.0;
	// 観察室の制約条件を設定します。
		for(;;)
		{
			// 観察室の看護師数
			if( plfArg[3]*plfArg[21] <= 25 )
			{
				// 観察室の部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[3] > 14.0 ) plfArg[3] = 14.0;
				if( plfArg[3] < 0.0 ) plfArg[3] = 0.0;
				// 観察室に所属する看護師は2人以下とします。
				// 公刊文献で1例だけではあるが、記載されており、算出も可能であったのでそれをベースに設定。
				if( plfArg[3] != 0.0 )
				{
					if( plfArg[21] < 1.0 ) plfArg[21] = 1.0;
					if( plfArg[21] > 2.0 ) plfArg[21] = 2.0;
				}
				else
				{
					// 観察室の部屋数が0の場合は看護師も割り当てがないことから0とします。
					plfArg[21] = 0.0;
				}
				break;
			}
			else
			{
				if( plfArg[3] > 14.0 ) plfArg[3] = 14.0;
				plfArg[3] -= 1.0;
				if( plfArg[3] < 0.0 ) plfArg[3] = 0.0;
				if( plfArg[3]*plfArg[21] <= 25 )
				{
					if( plfArg[21] < 1.0 ) plfArg[21] = 1.0;
					if( plfArg[21] > 2.0 ) plfArg[21] = 2.0;
				}
				else
				{
					if( plfArg[21] > 2.0 ) plfArg[21] = 2.0;
					plfArg[21] -= 1.0;
					if( plfArg[21] < 1.0 ) plfArg[21] = 1.0;
				}
			}
		}
	// 重症観察室の制約条件を設定します。
		for(;;)
		{
			// 重症観察室の看護師数
			if( plfArg[4]*plfArg[22] <= 25 )
			{
				// 重症観察室の部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[4] > 4.0 ) plfArg[4] = 4.0;
				if( plfArg[4] < 0.0 ) plfArg[4] = 0.0;
				// 重症観察室に所属する看護師は2人以下とします。
				// 公刊文献で1例だけではあるが、記載されており、算出も可能であったのでそれをベースに設定。
				if( plfArg[4] != 0.0 )
				{
					if( plfArg[22] < 1.0 ) plfArg[22] = 1.0;
					if( plfArg[22] > 2.0 ) plfArg[22] = 2.0;
				}
				else
				{
					// 重症観察室の部屋数が0の場合は看護師も割り当てがないことから0とします。
					plfArg[22] = 0.0;
				}
				break;
			}
			else
			{
				if( plfArg[4] > 4.0 ) plfArg[4] = 4.0;
				plfArg[4] -= 1.0;
				if( plfArg[4] < 0.0 ) plfArg[4] = 0.0;
				if( plfArg[4]*plfArg[22] <= 25 )
				{
					if( plfArg[22] < 1.0 ) plfArg[22] = 1.0;
					if( plfArg[22] > 2.0 ) plfArg[22] = 2.0;
				}
				else
				{
					if( plfArg[22] > 2.0 ) plfArg[22] = 2.0;
					plfArg[22] -= 1.0;
					if( plfArg[22] < 1.0 ) plfArg[22] = 1.0;
				}
			}
		}
	// ICUの制約条件を設定します。
		for(;;)
		{
			// ICUの医師数
			if( plfArg[5]*plfArg[23] <= 22 )
			{
				// ICUの部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[5] > 76.0 ) plfArg[5] = 76.0;
				if( plfArg[5] < 4.0 ) plfArg[5] = 4.0;
				// ICUの医師数を設定します。
				// 公刊文献上記載データから設定。
				if( plfArg[23] < 1.0 ) plfArg[23] = 1.0;
				if( plfArg[23] > 5.0 ) plfArg[23] = 5.0;
				break;
			}
			else
			{
				if( plfArg[5] > 76.0 ) plfArg[5] = 76.0;
				plfArg[5] -= 1.0;
				if( plfArg[5] < 4.0 ) plfArg[5] = 4.0;
				if( plfArg[5]*plfArg[23] <= 22 )
				{
					if( plfArg[23] < 1.0 ) plfArg[23] = 1.0;
					if( plfArg[23] > 5.0 ) plfArg[23] = 5.0;
				}
				else
				{
					if( plfArg[23] > 5.0 ) plfArg[23] = 5.0;
					plfArg[23] -= 1.0;
					if( plfArg[23] < 1.0 ) plfArg[23] = 1.0;
				}
			}
		}
		for(;;)
		{
			// ICUの看護師数
			if( plfArg[5]*plfArg[24] <= 152 )
			{
				// ICUの部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[5] > 76.0 ) plfArg[5] = 76.0;
				if( plfArg[5] < 4.0 ) plfArg[5] = 4.0;
				// ICUの1人当り看護師数を設定します。
				// ソースは医療法の医療点数に記載及び公刊文献を基に設定。
				if( plfArg[24] < 1.0 ) plfArg[24] = 1.0;
				if( plfArg[24] > 5.0 ) plfArg[24] = 5.0;
				break;
			}
			else
			{
				if( plfArg[5] > 76.0 ) plfArg[5] = 76.0;
				plfArg[5] -= 1.0;
				if( plfArg[5] < 4.0 ) plfArg[5] = 4.0;
				if( plfArg[5]*plfArg[24] <= 152 )
				{
					if( plfArg[24] < 1.0 ) plfArg[24] = 1.0;
					if( plfArg[24] > 5.0 ) plfArg[24] = 5.0;
				}
				else
				{
					if( plfArg[24] > 5.0 ) plfArg[24] = 5.0;
					plfArg[22] -= 1.0;
					if( plfArg[24] < 1.0 ) plfArg[24] = 1.0;
				}
			}
		}
	// HCUの制約条件を設定します。
		for(;;)
		{
			// HCUの医師数
			if( plfArg[6]*plfArg[25] <= 8 )
			{
				// HCUの部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[6] > 24.0 ) plfArg[6] = 24.0;
				if( plfArg[6] < 0.0 ) plfArg[6] = 0.0;
				// HCUの医師数を設定します。(HCUが存在している場合に設定します。)
				// 公刊文献上記載データから設定。
				if( plfArg[6] != 0.0 )
				{
					if( plfArg[25] < 1.0 ) plfArg[25] = 1.0;
					if( plfArg[25] > 5.0 ) plfArg[25] = 5.0;
				}
				else
				{
					// HCUの部屋数が0の場合は医師も割り当てがないことから0とします。
					plfArg[25] = 0.0;
				}
				break;
			}
			else
			{
				if( plfArg[6] > 24.0 ) plfArg[6] = 24.0;
				plfArg[6] -= 1.0;
				if( plfArg[6] < 0.0 ) plfArg[6] = 0.0;
				if( plfArg[6]*plfArg[25] <= 8 )
				{
					if( plfArg[25] < 1.0 ) plfArg[25] = 1.0;
					if( plfArg[25] > 5.0 ) plfArg[25] = 5.0;
				}
				else
				{
					if( plfArg[25] > 5.0 ) plfArg[25] = 5.0;
					plfArg[25] -= 1.0;
					if( plfArg[25] < 1.0 ) plfArg[25] = 1.0;
				}
			}
		}
		for(;;)
		{
			// HCUの看護師数
			if( plfArg[6]*plfArg[26] <= 96 )
			{
				// HCUの部屋数の設定をします。
				// 調査結果を基に設定します。
				if( plfArg[6] > 24.0 ) plfArg[6] = 24.0;
				if( plfArg[6] < 0.0 ) plfArg[6] = 0.0;
				// HCUの1人当りの看護師数を設定します。
				// ソースは医療法の医療点数に記載及び公刊文献を基に設定。
				if( plfArg[6] != 0.0 )
				{
					if( plfArg[26] < 1.0 ) plfArg[26] = 1.0;
					if( plfArg[26] > 4.0 ) plfArg[26] = 4.0;
				}
				else
				{
					// HCUの部屋数が0の場合は看護師も割り当てがないことから0とします。
					plfArg[26] = 0.0;
				}
				break;
			}
			else
			{
				if( plfArg[6] > 24.0 ) plfArg[6] = 24.0;
				plfArg[6] -= 1.0;
				if( plfArg[6] < 0.0 ) plfArg[6] = 0.0;
				if( plfArg[6]*plfArg[26] <= 96 )
				{
					if( plfArg[26] < 1.0 ) plfArg[26] = 1.0;
					if( plfArg[26] > 4.0 ) plfArg[26] = 4.0;
				}
				else
				{
					if( plfArg[26] > 4.0 ) plfArg[26] = 4.0;
					plfArg[26] -= 1.0;
					if( plfArg[26] < 1.0 ) plfArg[26] = 1.0;
				}
			}
		}
	// 一般病棟は制限なしとします。

		// 一般病棟の医師数を設定します。1病床に対して1人とします。
		// 実際には2人の固定値としています。
		if( plfArg[27] < 1.0 ) plfArg[27] = 1.0;
		if( plfArg[27] > 2.0 ) plfArg[27] = 2.0;

		for(;;)
		{
			// 一般病棟の看護師数
			if( plfArg[7]*plfArg[28] <= 1316 )
			{
				// 一般病棟の部屋数の設定をします。
				// 調査結果を基に設定します。
				if( plfArg[7] > 1296.0 ) plfArg[7] = 1296.0;
				if( plfArg[7] < 214.0 ) plfArg[7] = 214.0;
				if( plfArg[7] < 1.0 ) plfArg[7] = 1.0;
				// 一般病棟の看護師数を設定します。入院患者1(1病床)に対して最大看護師7人の設定とします。
				// ソースは医療法の医療点数に記載。
				if( plfArg[28] < 1.0 ) plfArg[28] = 1.0;
				if( plfArg[28] > 7.0 ) plfArg[28] = 7.0;
				break;
			}
			else
			{
				if( plfArg[7] > 1296.0 ) plfArg[7] = 1296.0;
				plfArg[7] -= 1.0;
				if( plfArg[7] < 214.0 ) plfArg[7] = 214.0;
				if( plfArg[7]*plfArg[28] <= 1316 )
				{
					if( plfArg[28] < 1.0 ) plfArg[28] = 1.0;
					if( plfArg[28] > 7.0 ) plfArg[28] = 7.0;
				}
				else
				{
					if( plfArg[28] > 7.0 ) plfArg[28] = 7.0;
					plfArg[28] -= 1.0;
					if( plfArg[28] < 1.0 ) plfArg[28] = 1.0;
				}
			}
		}
	// 待合室の制約条件を設定します。
		// 待合室は1部屋とします。
		plfArg[8] = 1;
		// 待合室の看護師の数を設定します。
		// 1人以上はいると予想されるので、そのように設定します。
		// 入院患者1(1病床)に対して最大看護師7人の設定とします。(医療法の医療点数を基に設定しました。)
		if( plfArg[29] < 1.0 ) plfArg[29] = 1.0;
		if( plfArg[29] > 7.0 ) plfArg[29] = 7.0;
	// X線室の制約条件を設定します。
		for(;;)
		{
			// 総数に関しても同様に調査結果を基に設定。
			if( plfArg[9]*plfArg[30] <= 22 )
			{
				// X線室を設定します。
				// 調査結果を基に設定。
				if( plfArg[9] > 18.0 ) plfArg[9] = 18.0;
				if( plfArg[9] < 1.0 ) plfArg[9] = 1.0;
				// 各検査室に所属する医療技師数は1人以上3人以下とします。
				// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
				// 公刊文献にも詳しい値が記載されていないことから。
				if( plfArg[30] < 1.0 ) plfArg[30] = 1.0;
				if( plfArg[30] > 3.0 ) plfArg[30] = 3.0;
				break;
			}
			else
			{
				if( plfArg[9] > 18.0 ) plfArg[9] = 18.0;
				plfArg[9] -= 1.0;
				if( plfArg[9] < 1.0 ) plfArg[9] = 1.0;
				if( plfArg[9]*plfArg[30] <= 22 )
				{
					if( plfArg[30] < 1.0 ) plfArg[30] = 1.0;
					if( plfArg[30] > 3.0 ) plfArg[30] = 3.0;
				}
				else
				{
					if( plfArg[30] > 3.0 ) plfArg[30] = 3.0;
					plfArg[30] -= 1.0;
					if( plfArg[30] < 1.0 ) plfArg[30] = 1.0;
				}
			}
		}
	// CT室の制約条件を設定します。
		for(;;)
		{
			// 総数に関しても同様に調査結果を基に設定。
			if( plfArg[10]*plfArg[31] <= 16 )
			{
				// CT室を設定します。
				// 調査結果を基に設定。
				if( plfArg[10] > 16.0 ) plfArg[10] = 16.0;
				if( plfArg[10] < 1.0 ) plfArg[10] = 1.0;
				// 各検査室に所属する医療技師数は1人以上3人以下とします。
				// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
				// 公刊文献にも詳しい値が記載されていないことから。
				if( plfArg[31] < 1.0 ) plfArg[31] = 1.0;
				if( plfArg[31] > 3.0 ) plfArg[31] = 3.0;
				break;
			}
			else
			{
				if( plfArg[10] > 16.0 ) plfArg[10] = 16.0;
				plfArg[10] -= 1.0;
				if( plfArg[10] < 1.0 ) plfArg[10] = 1.0;
				if( plfArg[10]*plfArg[31] <= 16 )
				{
					if( plfArg[31] < 1.0 ) plfArg[31] = 1.0;
					if( plfArg[31] > 3.0 ) plfArg[31] = 3.0;
				}
				else
				{
					if( plfArg[31] > 3.0 ) plfArg[31] = 3.0;
					plfArg[31] -= 1.0;
					if( plfArg[31] < 1.0 ) plfArg[31] = 1.0;
				}
			}
		}
	// MRI室の制約条件を設定します。
		for(;;)
		{
			// 総数に関しても同様に調査結果を基に設定。
			if( plfArg[11]*plfArg[32] <= 14 )
			{
				// MRI室を設定します。
				// 調査結果を基に設定。
				if( plfArg[11] > 7.0 ) plfArg[11] = 7.0;
				if( plfArg[11] < 1.0 ) plfArg[11] = 1.0;
				// 各検査室に所属する医療技師数は1人以上3人以下とします。
				// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
				// 公刊文献にも詳しい値が記載されていないことから。
				if( plfArg[32] < 1.0 ) plfArg[32] = 1.0;
				if( plfArg[32] > 3.0 ) plfArg[32] = 3.0;
				break;
			}
			else
			{
				if( plfArg[11] > 7.0 ) plfArg[11] = 7.0;
				plfArg[11] -= 1.0;
				if( plfArg[11] < 1.0 ) plfArg[11] = 1.0;
				if( plfArg[11]*plfArg[32] <= 14 )
				{
					if( plfArg[32] < 1.0 ) plfArg[32] = 1.0;
					if( plfArg[32] > 3.0 ) plfArg[32] = 3.0;
				}
				else
				{
					if( plfArg[32] > 3.0 ) plfArg[32] = 3.0;
					plfArg[32] -= 1.0;
					if( plfArg[32] < 1.0 ) plfArg[32] = 1.0;
				}
			}
		}
	// 血管造影室の制約条件を設定します。
		for(;;)
		{
			// 総数に関しても同様に調査結果を基に設定。
			if( plfArg[12]*plfArg[33] <= 14 )
			{
				// 血管造影室を設定します。
				// 調査結果を基に設定。
				if( plfArg[12] > 16.0 ) plfArg[12] = 16.0;
				if( plfArg[12] < 1.0 ) plfArg[12] = 1.0;
				// 各検査室に所属する医療技師数は1人以上3人以下とします。
				// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
				// 公刊文献にも詳しい値が記載されていないことから。
				if( plfArg[33] < 1.0 ) plfArg[33] = 1.0;
				if( plfArg[33] > 3.0 ) plfArg[33] = 3.0;
				break;
			}
			else
			{
				if( plfArg[12] > 16.0 ) plfArg[12] = 16.0;
				plfArg[12] -= 1.0;
				if( plfArg[12] < 1.0 ) plfArg[12] = 1.0;
				if( plfArg[12]*plfArg[33] <= 14 )
				{
					if( plfArg[33] < 1.0 ) plfArg[33] = 1.0;
					if( plfArg[33] > 3.0 ) plfArg[33] = 3.0;
				}
				else
				{
					if( plfArg[33] > 3.0 ) plfArg[33] = 3.0;
					plfArg[33] -= 1.0;
					if( plfArg[33] < 1.0 ) plfArg[33] = 1.0;
				}
			}
		}
	// FAST室の制約条件を設定します。
		for(;;)
		{
			// 総数に関しても同様に調査結果を基に設定。
			if( plfArg[13]*plfArg[34] <= 26 )
			{
				// 超音波室を設定します。
				// 調査結果を基に設定。
				if( plfArg[13] > 8.0 ) plfArg[13] = 8.0;
				if( plfArg[13] < 1.0 ) plfArg[13] = 1.0;
				// 各検査室に所属する医療技師数は1人以上3人以下とします。
				// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
				// 公刊文献にも詳しい値が記載されていないことから。
				if( plfArg[34] < 1.0 ) plfArg[34] = 1.0;
				if( plfArg[34] > 3.0 ) plfArg[34] = 3.0;
				break;
			}
			else
			{
				if( plfArg[13] > 8.0 ) plfArg[13] = 8.0;
				plfArg[13] -= 1.0;
				if( plfArg[13] < 1.0 ) plfArg[13] = 1.0;
				if( plfArg[13]*plfArg[34] <= 26 )
				{
					if( plfArg[34] < 1.0 ) plfArg[34] = 1.0;
					if( plfArg[34] > 3.0 ) plfArg[34] = 3.0;
				}
				else
				{
					if( plfArg[34] > 3.0 ) plfArg[34] = 3.0;
					plfArg[34] -= 1.0;
					if( plfArg[34] < 1.0 ) plfArg[34] = 1.0;
				}
			}
		}

	// 部屋を構成するエージェント数の設定

		// 年間で来院する患者の人数の算出が必要です。
		// 筑波メディカルセンターの場合は・・・
		// 聖隷浜松病院の場合は・・・19379人
		// 各診察室の医師数は最大でも2人くらいに設定します。
		// 算出方法は医療法に記載のものから算出。
//		// 患者の総数を取得します。
//		lfPatientsNum = 19379;
//		// 必要医師数を算出します。
//		lfDoctorsNum = lfPatientsNum*0.0625+lfPatientsNum*1.0/16.0;
//		// 必要看護師数を算出します。
//		lfNursesNum = lfPatientsNum*1.0/3.0+lfPatientsNum*1.0/30.0;
//		// 総部屋数を算出します。
//		lfTotalRoomNum = plfArg[0]+plfArg[1]+plfArg[2]+plfArg[3]+plfArg[4]+plfArg[5]+plfArg[6]+plfArg[7]+plfArg[8]+plfArg[9]+plfArg[10]+plfArg[11]+plfArg[12]+plfArg[13];
//
//		lfOneRoomDoctorNum			 = lfDoctorsNum / lfTotalRoomNum;
//		lfOneRoomNurseNum			 = lfNursesNum / lfTotalRoomNum;
//		lfOneRoomClinicalEngineerNum = lfClinicalEngineersNum / lfTotalRoomNum;

	}

	/**
	 * <PRE>
	 *   救急部門用の一般的な制約条件設定
	 * </PRE>
	 * @param plfArg	制約条件用引数
	 */
	private void vSetEDCondition2( double[] plfArg )
	{
//		double lfPatientsNum;
//		double lfDoctorsNum;
//		double lfNursesNum;
//		double lfClinicalEngineersNum;
//		double lfTotalRoomNum;
//		double lfOneRoomDoctorNum;
//		double lfOneRoomNurseNum;
//		double lfOneRoomClinicalEngineerNum;
		int i;

	// 部屋の設定

	// 診察室の制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// 診察室の医師数
			if( plfArg[0]*plfArg[14] <= 42 )
			{
				// 診察室の部屋数を設定します。
				// 調査結果を基に設定。
				if( plfArg[0] > 20.0 ) plfArg[0] = 20.0;
				if( plfArg[0] < 4.0 ) plfArg[0] = 4.0;
				// 診察室の医師数を設定します。
				// 現実的に考えて1部屋に1人が担当医だと考えられるので。2人は多く見積もった。
				if( plfArg[14] < 1.0 ) plfArg[14] = 1.0;
				if( plfArg[14] > 2.0 ) plfArg[14] = 2.0;
				break;
			}
			else
			{
				if( plfArg[0] > 20.0 ) plfArg[0] = 20.0;
				plfArg[0] -= 1.0;
				if( plfArg[0] < 4.0 ) plfArg[0] = 4.0;
				if( plfArg[0]*plfArg[14] <= 42 )
				{
					if( plfArg[14] < 1.0 ) plfArg[14] = 1.0;
					if( plfArg[14] > 2.0 ) plfArg[14] = 2.0;
				}
				else
				{
					if( plfArg[14] > 2.0 ) plfArg[14] = 2.0;
					plfArg[14] -= 1.0;
					if( plfArg[14] < 1.0 ) plfArg[14] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("診察室医師数：適切な値を発見できませんでした。");
		for(i = 0; i < 1000; i++ )
		{
			// 診察室の看護師数
			if( plfArg[0]*plfArg[15] <= 42 )
			{
				// 診察室の部屋数を設定します。
				// 調査結果を基に設定。
				if( plfArg[0] > 20.0 ) plfArg[0] = 20.0;
				if( plfArg[0] < 4.0 ) plfArg[0] = 4.0;
				// 各診察室の看護師数は最大2人に設定します。
				// 現実的に考えてサポートで1人いると思われるが、多く見積もって2人とした。
				if( plfArg[15] < 0.0 ) plfArg[15] = 0.0;
				if( plfArg[15] > 2.0 ) plfArg[15] = 2.0;
				break;
			}
			else
			{
				if( plfArg[0] > 20.0 ) plfArg[0] = 20.0;
				plfArg[0] -= 1.0;
				if( plfArg[0] < 4.0 ) plfArg[0] = 4.0;
				if( plfArg[0]*plfArg[15] <= 42 )
				{
					if( plfArg[15] < 0.0 ) plfArg[15] = 0.0;
					if( plfArg[15] > 2.0 ) plfArg[15] = 2.0;
				}
				else
				{
					if( plfArg[15] > 2.0 ) plfArg[15] = 2.0;
					plfArg[15] -= 1.0;
					if( plfArg[15] < 0.0 ) plfArg[15] = 0.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("診察室看護師数：適切な値を発見できませんでした。");
	// 手術室の制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// 手術室の医師数
			if( plfArg[1]*plfArg[16] <= 15 )
			{
				// 手術室の部屋数を設定します。
				// 調査結果を基に設定。
				if( plfArg[1] > 15.0 ) plfArg[1] = 15.0;
				if( plfArg[1] < 4.0 ) plfArg[1] = 4.0;
				// 各手術室及び初療室の医師数は最大でも3人くらいと設定します。
				if( plfArg[16] < 1.0 ) plfArg[16] = 1.0;
				if( plfArg[16] > 3.0 ) plfArg[16] = 3.0;
				break;
			}
			else
			{
				if( plfArg[1] > 15.0 ) plfArg[1] = 15.0;
				plfArg[1] -= 1.0;
				if( plfArg[1] < 4.0 ) plfArg[1] = 4.0;
				if( plfArg[1]*plfArg[16] <= 15 )
				{
					if( plfArg[16] < 1.0 ) plfArg[16] = 1.0;
					if( plfArg[16] > 3.0 ) plfArg[16] = 3.0;
				}
				else
				{
					if( plfArg[16] > 3.0 ) plfArg[16] = 3.0;
					plfArg[16] -= 1.0;
					if( plfArg[16] < 1.0 ) plfArg[16] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("手術室医師数：適切な値を発見できませんでした。");
		// 手術室の看護師数
		for(i = 0; i < 1000; i++ )
		{
			if( plfArg[1]*plfArg[17] <= 82 )
			{
				// 手術室の部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[1] > 23.0 ) plfArg[1] = 23.0;
				if( plfArg[1] < 4.0 ) plfArg[1] = 4.0;
				// 各手術室、各初療室の看護師数は最大で4人くらいと設定します。
				// 公刊文献を調べて3例ほどから。手術室が1部屋当りの人数を算出することができたのでそれを参考に設定。
				if( plfArg[17] < 1.0 ) plfArg[17] = 1.0;
				if( plfArg[17] > 3.0 ) plfArg[17] = 3.0;
				break;
			}
			else
			{
				if( plfArg[1] > 23.0 ) plfArg[1] = 23.0;
				plfArg[1] -= 1.0;
				if( plfArg[1] < 4.0 ) plfArg[1] = 4.0;
				if( plfArg[1]*plfArg[17] <= 82 )
				{
					if( plfArg[17] < 1.0 ) plfArg[17] = 1.0;
					if( plfArg[17] > 3.0 ) plfArg[17] = 3.0;
				}
				else
				{
					if( plfArg[17] > 3.0 ) plfArg[17] = 3.0;
					plfArg[17] -= 1.0;
					if( plfArg[17] < 1.0 ) plfArg[17] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("手術室看護師数：適切な値を発見できませんでした。");
	// 初療室の制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// 初療室の医師数
			if( plfArg[2]*plfArg[18] <= 42 )
			{
				// 初療室の部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[2] > 14.0 ) plfArg[2] = 14.0;
				if( plfArg[2] < 2.0 ) plfArg[2] = 2.0;
				// 各手術室及び初療室の医師数は最大でも3人くらいと設定します。
				if( plfArg[18] < 1.0 ) plfArg[18] = 1.0;
				if( plfArg[18] > 3.0 ) plfArg[18] = 3.0;
				break;
			}
			else
			{
				if( plfArg[2] > 14.0 ) plfArg[2] = 14.0;
				plfArg[2] -= 1.0;
				if( plfArg[2] < 2.0 ) plfArg[2] = 2.0;
				if( plfArg[2]*plfArg[18] <= 42 )
				{
					if( plfArg[18] < 1.0 ) plfArg[18] = 1.0;
//					if( plfArg[18] > 3.0 ) plfArg[18] = 3.0;
				}
				else
				{
//					if( plfArg[18] > 3.0 ) plfArg[18] = 3.0;
					plfArg[18] -= 1.0;
					if( plfArg[18] < 1.0 ) plfArg[18] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("初療室医師数：適切な値を発見できませんでした。");
		for(i = 0; i < 1000; i++ )
		{
			// 初療室の看護師数
			if( plfArg[2]*plfArg[19] <= 31 )
			{
				// 初療室の部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[2] > 14.0 ) plfArg[2] = 14.0;
				if( plfArg[2] < 2.0 ) plfArg[2] = 2.0;
				// 各手術室、各初療室の看護師数は最大で4人くらいと設定します。
				// 公刊文献を調べて3例ほどから。手術室が1部屋当りの人数を算出することができたのでそれを参考に設定。
				if( plfArg[19] < 1.0 ) plfArg[19] = 1.0;
//				if( plfArg[19] > 4.0 ) plfArg[19] = 4.0;
				break;
			}
			else
			{
				if( plfArg[2] > 14.0 ) plfArg[2] = 14.0;
				plfArg[2] -= 1.0;
				if( plfArg[2] < 2.0 ) plfArg[2] = 2.0;
				if( plfArg[2]*plfArg[19] <= 31 )
				{
					if( plfArg[19] < 1.0 ) plfArg[19] = 1.0;
//					if( plfArg[19] > 4.0 ) plfArg[19] = 4.0;
				}
				else
				{
//					if( plfArg[19] > 4.0 ) plfArg[19] = 4.0;
					plfArg[19] -= 1.0;
					if( plfArg[19] < 1.0 ) plfArg[19] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("初療室看護師数：適切な値を発見できませんでした。");
		// 初療室の医療技師数
		if( plfArg[20] < 1.0 ) plfArg[20] = 1.0;
		if( plfArg[20] > 3.0 ) plfArg[20] = 3.0;
	// 観察室の制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// 観察室の看護師数
			if( plfArg[3]*plfArg[21] <= 25 )
			{
				// 観察室の部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[3] > 14.0 ) plfArg[3] = 14.0;
				if( plfArg[3] < 0.0 ) plfArg[3] = 0.0;
				// 観察室に所属する看護師は2人以下とします。
				// 公刊文献で1例だけではあるが、記載されており、算出も可能であったのでそれをベースに設定。
				if( plfArg[3] != 0.0 )
				{
					if( plfArg[21] < 1.0 ) plfArg[21] = 1.0;
//					if( plfArg[21] > 2.0 ) plfArg[21] = 2.0;
				}
				else
				{
					// 観察室の部屋数が0の場合は看護師も割り当てがないことから0とします。
					plfArg[21] = 0.0;
				}
				break;
			}
			else
			{
				if( plfArg[3] > 14.0 ) plfArg[3] = 14.0;
				plfArg[3] -= 1.0;
				if( plfArg[3] < 0.0 ) plfArg[3] = 0.0;
				if( plfArg[3]*plfArg[21] <= 25 )
				{
					if( plfArg[21] < 1.0 ) plfArg[21] = 1.0;
//					if( plfArg[21] > 2.0 ) plfArg[21] = 2.0;
				}
				else
				{
//					if( plfArg[21] > 2.0 ) plfArg[21] = 2.0;
					plfArg[21] -= 1.0;
					if( plfArg[21] < 1.0 ) plfArg[21] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("観察室看護師数：適切な値を発見できませんでした。");
	// 重症観察室の制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// 重症観察室の看護師数
			if( plfArg[4]*plfArg[22] <= 25 )
			{
				// 重症観察室の部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[4] > 4.0 ) plfArg[4] = 4.0;
				if( plfArg[4] < 0.0 ) plfArg[4] = 0.0;
				// 重症観察室に所属する看護師は2人以下とします。
				// 公刊文献で1例だけではあるが、記載されており、算出も可能であったのでそれをベースに設定。
				if( plfArg[4] != 0.0 )
				{
					if( plfArg[22] < 1.0 ) plfArg[22] = 1.0;
					if( plfArg[22] > 2.0 ) plfArg[22] = 2.0;
				}
				else
				{
					// 重症観察室の部屋数が0の場合は看護師も割り当てがないことから0とします。
					plfArg[22] = 0.0;
				}
				break;
			}
			else
			{
				if( plfArg[4] > 4.0 ) plfArg[4] = 4.0;
				plfArg[4] -= 1.0;
				if( plfArg[4] < 0.0 ) plfArg[4] = 0.0;
				if( plfArg[4]*plfArg[22] <= 25 )
				{
					if( plfArg[22] < 1.0 ) plfArg[22] = 1.0;
//					if( plfArg[22] > 2.0 ) plfArg[22] = 2.0;
				}
				else
				{
//					if( plfArg[22] > 2.0 ) plfArg[22] = 2.0;
					plfArg[22] -= 1.0;
					if( plfArg[22] < 1.0 ) plfArg[22] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("重症観察室看護師数：適切な値を発見できませんでした。");
	// ICUの制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// ICUの医師数
			if( plfArg[5]*plfArg[23] <= 22 )
			{
				// ICUの部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[5] > 76.0 ) plfArg[5] = 76.0;
				if( plfArg[5] < 4.0 ) plfArg[5] = 4.0;
				// ICUの医師数を設定します。
				// 公刊文献上記載データから設定。
				if( plfArg[23] < 1.0 ) plfArg[23] = 1.0;
//				if( plfArg[23] > 5.0 ) plfArg[23] = 5.0;
				break;
			}
			else
			{
				if( plfArg[5] > 76.0 ) plfArg[5] = 76.0;
				plfArg[5] -= 1.0;
				if( plfArg[5] < 4.0 ) plfArg[5] = 4.0;
				if( plfArg[5]*plfArg[23] <= 22 )
				{
					if( plfArg[23] < 1.0 ) plfArg[23] = 1.0;
//					if( plfArg[23] > 5.0 ) plfArg[23] = 5.0;
				}
				else
				{
//					if( plfArg[23] > 5.0 ) plfArg[23] = 5.0;
					plfArg[23] -= 1.0;
					if( plfArg[23] < 1.0 ) plfArg[23] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("ICU医師数：適切な値を発見できませんでした。");
		for(i = 0; i < 1000; i++ )
		{
			// ICUの看護師数
			if( plfArg[5]*plfArg[24] <= 152 )
			{
				// ICUの部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[5] > 76.0 ) plfArg[5] = 76.0;
				if( plfArg[5] < 4.0 ) plfArg[5] = 4.0;
				// ICUの1人当り看護師数を設定します。
				// ソースは医療法の医療点数に記載及び公刊文献を基に設定。
				if( plfArg[24] < 1.0 ) plfArg[24] = 1.0;
//				if( plfArg[24] > 5.0 ) plfArg[24] = 5.0;
				break;
			}
			else
			{
				if( plfArg[5] > 76.0 ) plfArg[5] = 76.0;
				plfArg[5] -= 1.0;
				if( plfArg[5] < 4.0 ) plfArg[5] = 4.0;
				if( plfArg[5]*plfArg[24] <= 152 )
				{
					if( plfArg[24] < 1.0 ) plfArg[24] = 1.0;
//					if( plfArg[24] > 5.0 ) plfArg[24] = 5.0;
				}
				else
				{
//					if( plfArg[24] > 5.0 ) plfArg[24] = 5.0;
					plfArg[24] -= 1.0;
					if( plfArg[24] < 1.0 ) plfArg[24] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("ICU看護師数：適切な値を発見できませんでした。");
	// HCUの制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// HCUの医師数
			if( plfArg[6]*plfArg[25] <= 8 )
			{
				// HCUの部屋数を設定します。
				// 調査結果を基に設定します。
				if( plfArg[6] > 24.0 ) plfArg[6] = 24.0;
				if( plfArg[6] < 0.0 ) plfArg[6] = 0.0;
				// HCUの医師数を設定します。(HCUが存在している場合に設定します。)
				// 公刊文献上記載データから設定。
				if( plfArg[6] != 0.0 )
				{
					if( plfArg[25] < 1.0 ) plfArg[25] = 1.0;
					if( plfArg[25] > 5.0 ) plfArg[25] = 5.0;
				}
				else
				{
					// HCUの部屋数が0の場合は医師も割り当てがないことから0とします。
					plfArg[25] = 0.0;
				}
				break;
			}
			else
			{
				if( plfArg[6] > 24.0 ) plfArg[6] = 24.0;
				plfArg[6] -= 1.0;
				if( plfArg[6] < 0.0 ) plfArg[6] = 0.0;
				if( plfArg[6]*plfArg[25] <= 8 )
				{
					if( plfArg[25] < 1.0 ) plfArg[25] = 1.0;
					if( plfArg[25] > 5.0 ) plfArg[25] = 5.0;
				}
				else
				{
					if( plfArg[25] > 5.0 ) plfArg[25] = 5.0;
					plfArg[25] -= 1.0;
					if( plfArg[25] < 1.0 ) plfArg[25] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("HCU医師数：適切な値を発見できませんでした。");
		for(i = 0; i < 1000; i++ )
		{
			// HCUの看護師数
			if( plfArg[6]*plfArg[26] <= 96 )
			{
				// HCUの部屋数の設定をします。
				// 調査結果を基に設定します。
				if( plfArg[6] > 24.0 ) plfArg[6] = 24.0;
				if( plfArg[6] < 0.0 ) plfArg[6] = 0.0;
				// HCUの1人当りの看護師数を設定します。
				// ソースは医療法の医療点数に記載及び公刊文献を基に設定。
				if( plfArg[6] != 0.0 )
				{
					if( plfArg[26] < 1.0 ) plfArg[26] = 1.0;
//					if( plfArg[26] > 4.0 ) plfArg[26] = 4.0;
				}
				else
				{
					// HCUの部屋数が0の場合は看護師も割り当てがないことから0とします。
					plfArg[26] = 0.0;
				}
				break;
			}
			else
			{
				if( plfArg[6] > 24.0 ) plfArg[6] = 24.0;
				plfArg[6] -= 1.0;
				if( plfArg[6] < 0.0 ) plfArg[6] = 0.0;
				if( plfArg[6]*plfArg[26] <= 96 )
				{
					if( plfArg[26] < 1.0 ) plfArg[26] = 1.0;
//					if( plfArg[26] > 4.0 ) plfArg[26] = 4.0;
				}
				else
				{
//					if( plfArg[26] > 4.0 ) plfArg[26] = 4.0;
					plfArg[26] -= 1.0;
					if( plfArg[26] < 1.0 ) plfArg[26] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("HCU看護師数：適切な値を発見できませんでした。");
	// 一般病棟は制限なしとします。

		// 一般病棟の医師数を設定します。1病床に対して1人とします。
		// 実際には2人の固定値としています。
		if( plfArg[27] < 1.0 ) plfArg[27] = 1.0;
		if( plfArg[27] > 2.0 ) plfArg[27] = 2.0;

		for(i = 0; i < 1000; i++ )
		{
			// 一般病棟の看護師数
			if( plfArg[7]*plfArg[28] <= 1316 )
			{
				// 一般病棟の部屋数の設定をします。
				// 調査結果を基に設定します。
				if( plfArg[7] > 1296.0 ) plfArg[7] = 1296.0;
//				if( plfArg[7] < 214.0 ) plfArg[7] = 214.0;
				if( plfArg[7] < 1.0 ) plfArg[7] = 1.0;
				// 一般病棟の看護師数を設定します。入院患者1(1病床)に対して最大看護師7人の設定とします。
				// ソースは医療法の医療点数に記載。
				if( plfArg[28] < 1.0 ) plfArg[28] = 1.0;
//				if( plfArg[28] > 7.0 ) plfArg[28] = 7.0;
				break;
			}
			else
			{
				if( plfArg[7] > 1296.0 ) plfArg[7] = 1296.0;
				plfArg[7] -= 1.0;
//				if( plfArg[7] < 214.0 ) plfArg[7] = 214.0;
				if( plfArg[7]*plfArg[28] <= 1316 )
				{
					if( plfArg[28] < 1.0 ) plfArg[28] = 1.0;
//					if( plfArg[28] > 7.0 ) plfArg[28] = 7.0;
				}
				else
				{
//					if( plfArg[28] > 7.0 ) plfArg[28] = 7.0;
					plfArg[28] -= 1.0;
					if( plfArg[28] < 1.0 ) plfArg[28] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("一般病棟看護師数：適切な値を発見できませんでした。");
	// 待合室の制約条件を設定します。
		// 待合室は1部屋とします。
		plfArg[8] = 1;
		// 待合室の看護師の数を設定します。
		// 1人以上はいると予想されるので、そのように設定します。
		// 入院患者1(1病床)に対して最大看護師7人の設定とします。(医療法の医療点数を基に設定しました。)
		if( plfArg[29] < 1.0 ) plfArg[29] = 1.0;
		if( plfArg[29] > 7.0 ) plfArg[29] = 7.0;
	// X線室の制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// 総数に関しても同様に調査結果を基に設定。
			if( plfArg[9]*plfArg[30] <= 22 )
			{
				// X線室を設定します。
				// 調査結果を基に設定。
				if( plfArg[9] > 18.0 ) plfArg[9] = 18.0;
				if( plfArg[9] < 1.0 ) plfArg[9] = 1.0;
				// 各検査室に所属する医療技師数は1人以上3人以下とします。
				// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
				// 公刊文献にも詳しい値が記載されていないことから。
				if( plfArg[30] < 1.0 ) plfArg[30] = 1.0;
				if( plfArg[30] > 3.0 ) plfArg[30] = 3.0;
				break;
			}
			else
			{
				if( plfArg[9] > 18.0 ) plfArg[9] = 18.0;
				plfArg[9] -= 1.0;
				if( plfArg[9] < 1.0 ) plfArg[9] = 1.0;
				if( plfArg[9]*plfArg[30] <= 22 )
				{
					if( plfArg[30] < 1.0 ) plfArg[30] = 1.0;
					if( plfArg[30] > 3.0 ) plfArg[30] = 3.0;
				}
				else
				{
					if( plfArg[30] > 3.0 ) plfArg[30] = 3.0;
					plfArg[30] -= 1.0;
					if( plfArg[30] < 1.0 ) plfArg[30] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("X線室医療技師数：適切な値を発見できませんでした。");
	// CT室の制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// 総数に関しても同様に調査結果を基に設定。
			if( plfArg[10]*plfArg[31] <= 16 )
			{
				// CT室を設定します。
				// 調査結果を基に設定。
				if( plfArg[10] > 16.0 ) plfArg[10] = 16.0;
				if( plfArg[10] < 1.0 ) plfArg[10] = 1.0;
				// 各検査室に所属する医療技師数は1人以上3人以下とします。
				// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
				// 公刊文献にも詳しい値が記載されていないことから。
				if( plfArg[31] < 1.0 ) plfArg[31] = 1.0;
				if( plfArg[31] > 3.0 ) plfArg[31] = 3.0;
				break;
			}
			else
			{
				if( plfArg[10] > 16.0 ) plfArg[10] = 16.0;
				plfArg[10] -= 1.0;
				if( plfArg[10] < 1.0 ) plfArg[10] = 1.0;
				if( plfArg[10]*plfArg[31] <= 16 )
				{
					if( plfArg[31] < 1.0 ) plfArg[31] = 1.0;
					if( plfArg[31] > 3.0 ) plfArg[31] = 3.0;
				}
				else
				{
					if( plfArg[31] > 3.0 ) plfArg[31] = 3.0;
					plfArg[31] -= 1.0;
					if( plfArg[31] < 1.0 ) plfArg[31] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("CT室医療技師数：適切な値を発見できませんでした。");
	// MRI室の制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// 総数に関しても同様に調査結果を基に設定。
			if( plfArg[11]*plfArg[32] <= 14 )
			{
				// MRI室を設定します。
				// 調査結果を基に設定。
				if( plfArg[11] > 7.0 ) plfArg[11] = 7.0;
				if( plfArg[11] < 1.0 ) plfArg[11] = 1.0;
				// 各検査室に所属する医療技師数は1人以上3人以下とします。
				// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
				// 公刊文献にも詳しい値が記載されていないことから。
				if( plfArg[32] < 1.0 ) plfArg[32] = 1.0;
				if( plfArg[32] > 3.0 ) plfArg[32] = 3.0;
				break;
			}
			else
			{
				if( plfArg[11] > 7.0 ) plfArg[11] = 7.0;
				plfArg[11] -= 1.0;
				if( plfArg[11] < 1.0 ) plfArg[11] = 1.0;
				if( plfArg[11]*plfArg[32] <= 14 )
				{
					if( plfArg[32] < 1.0 ) plfArg[32] = 1.0;
					if( plfArg[32] > 3.0 ) plfArg[32] = 3.0;
				}
				else
				{
					if( plfArg[32] > 3.0 ) plfArg[32] = 3.0;
					plfArg[32] -= 1.0;
					if( plfArg[32] < 1.0 ) plfArg[32] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("MRI室医療技師数：適切な値を発見できませんでした。");
	// 血管造影室の制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// 総数に関しても同様に調査結果を基に設定。
			if( plfArg[12]*plfArg[33] <= 14 )
			{
				// 血管造影室を設定します。
				// 調査結果を基に設定。
				if( plfArg[12] > 16.0 ) plfArg[12] = 16.0;
				if( plfArg[12] < 1.0 ) plfArg[12] = 1.0;
				// 各検査室に所属する医療技師数は1人以上3人以下とします。
				// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
				// 公刊文献にも詳しい値が記載されていないことから。
				if( plfArg[33] < 1.0 ) plfArg[33] = 1.0;
				if( plfArg[33] > 3.0 ) plfArg[33] = 3.0;
				break;
			}
			else
			{
				if( plfArg[12] > 16.0 ) plfArg[12] = 16.0;
				plfArg[12] -= 1.0;
				if( plfArg[12] < 1.0 ) plfArg[12] = 1.0;
				if( plfArg[12]*plfArg[33] <= 14 )
				{
					if( plfArg[33] < 1.0 ) plfArg[33] = 1.0;
					if( plfArg[33] > 3.0 ) plfArg[33] = 3.0;
				}
				else
				{
					if( plfArg[33] > 3.0 ) plfArg[33] = 3.0;
					plfArg[33] -= 1.0;
					if( plfArg[33] < 1.0 ) plfArg[33] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("血管造影室医療技師数：適切な値を発見できませんでした。");
	// FAST室の制約条件を設定します。
		for(i = 0; i < 1000; i++ )
		{
			// 総数に関しても同様に調査結果を基に設定。
			if( plfArg[13]*plfArg[34] <= 26 )
			{
				// 超音波室を設定します。
				// 調査結果を基に設定。
				if( plfArg[13] > 8.0 ) plfArg[13] = 8.0;
				if( plfArg[13] < 1.0 ) plfArg[13] = 1.0;
				// 各検査室に所属する医療技師数は1人以上3人以下とします。
				// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
				// 公刊文献にも詳しい値が記載されていないことから。
				if( plfArg[34] < 1.0 ) plfArg[34] = 1.0;
				if( plfArg[34] > 3.0 ) plfArg[34] = 3.0;
				break;
			}
			else
			{
				if( plfArg[13] > 8.0 ) plfArg[13] = 8.0;
				plfArg[13] -= 1.0;
				if( plfArg[13] < 1.0 ) plfArg[13] = 1.0;
				if( plfArg[13]*plfArg[34] <= 26 )
				{
					if( plfArg[34] < 1.0 ) plfArg[34] = 1.0;
					if( plfArg[34] > 3.0 ) plfArg[34] = 3.0;
				}
				else
				{
					if( plfArg[34] > 3.0 ) plfArg[34] = 3.0;
					plfArg[34] -= 1.0;
					if( plfArg[34] < 1.0 ) plfArg[34] = 1.0;
				}
			}
		}
		if( i == 1000 ) System.out.println("FAST室医療技師数：適切な値を発見できませんでした。");
	}

	/**
	 * <PRE>
	 *   救急部門用の一般的な制約条件設定
	 * </PRE>
	 * @param plfArg	制約条件用引数
	 */
	private void vSetEDCalibrationCondition( double[] plfArg )
	{
	// 部屋の設定
		double lfMinWeight = 0.9;
		double lfMaxWeight = 1.1;
		double lfMinRangeTotalDoctor = 0.0;
		double lfMaxRangeTotalDoctor = 0.0;
		double lfMinRangeTotalNurse = 0.0;
		double lfMaxRangeTotalNurse = 0.0;
		double lfMinRangeTotalClinicalEngineer = 0.0;
		double lfMaxRangeTotalClinicalEngineer = 0.0;
		double lfMinRangeRoom = 0.0;
		double lfMaxRangeRoom = 0.0;
		double lfMinRangeDoctor = 0.0;
		double lfMaxRangeDoctor = 0.0;
		double lfMinRangeNurse = 0.0;
		double lfMaxRangeNurse = 0.0;
		double lfMinRangeClinicalEngineer = 0.0;
		double lfMaxRangeClinicalEngineer = 0.0;
		double lfCompRange1 = 0.0;
		double lfCompRange2 = 0.0;
		boolean bCompRangeFlag1 = true;
		boolean bCompRangeFlag2 = true;
		boolean bCompRangeFlag3 = true;
		boolean bRet = false;

	// 診察室の制約条件を設定します。
		// 診察室の医師数
		// 診察室の看護師数
//		lfMinRangeTotalDoctor = lfMinWeight*12;
//		lfMaxRangeTotalDoctor = lfMaxWeight*12;
//		lfMinRangeTotalNurse = lfMinWeight*30;
//		lfMaxRangeTotalNurse = lfMaxWeight*30;
//		lfMinRangeRoom = 1;
//		lfMaxRangeRoom = 12;
//		lfMaxRangeDoctor = 3;
//		lfMinRangeDoctor = 1;
//		lfMinRangeNurse = 1;
//		lfMaxRangeNurse = 30;

		lfMinRangeTotalDoctor = invParam.lfGetConsultationRoomDoctorNumMinWeight()*invParam.iGetConsultationRoomTotalDoctorNum();
		lfMaxRangeTotalDoctor = invParam.lfGetConsultationRoomDoctorNumMaxWeight()*invParam.iGetConsultationRoomTotalDoctorNum();
		lfMinRangeTotalNurse = invParam.lfGetConsultationRoomNurseNumMinWeight()*invParam.iGetConsultationRoomTotalNurseNum();
		lfMaxRangeTotalNurse = invParam.lfGetConsultationRoomNurseNumMaxWeight()*invParam.iGetConsultationRoomTotalNurseNum();
		lfMinRangeRoom = invParam.iGetConsultationRoomNumMin();
		lfMaxRangeRoom = invParam.iGetConsultationRoomNumMax();;
		lfMinRangeDoctor = invParam.iGetConsultationRoomDoctorNumMin();
		lfMaxRangeDoctor = invParam.iGetConsultationRoomDoctorNumMax();
		lfMinRangeNurse = invParam.iGetConsultationRoomNurseNumMin();
		lfMaxRangeNurse = invParam.iGetConsultationRoomNurseNumMax();

		// 診察室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[0] = lfLimitter( plfArg[0], lfMaxRangeRoom, lfMinRangeRoom );
		// 診察室の医師数を設定します。
		// 現実的に考えて1部屋に1人が担当医だと考えられるので。2人は多く見積もった。
		plfArg[14] = lfLimitter( plfArg[14], lfMaxRangeDoctor, lfMinRangeDoctor );
		// 診察室の看護師数を設定します。
		// 最大数を設定できるようにします。
		plfArg[15] = lfLimitter( plfArg[15], lfMaxRangeNurse, lfMinRangeNurse );

		bRet = bUpdateRoomAgent( plfArg, 0, 14, 15, lfMinRangeTotalDoctor, lfMaxRangeTotalDoctor, lfMinRangeTotalNurse, lfMaxRangeTotalNurse,
				lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeDoctor, lfMaxRangeDoctor, lfMinRangeNurse, lfMaxRangeNurse );
//		if( bRet == true ) System.out.println("診察室OK");
//		else			   System.out.println("診察室NG");

	// 手術室の制約条件を設定します。
		// 手術室の医師数を設定します。
		// 手術室の看護師数を設定します。
//		lfMinRangeTotalDoctor = lfMinWeight*15;
//		lfMaxRangeTotalDoctor = lfMaxWeight*15;
//		lfMinRangeTotalNurse = lfMinWeight*82;
//		lfMaxRangeTotalNurse = lfMaxWeight*82;
//		lfMinRangeRoom = 1;
//		lfMaxRangeRoom = 15;
//		lfMinRangeDoctor = 1;
//		lfMaxRangeDoctor = 4;
//		lfMinRangeNurse = 1;
//		lfMaxRangeNurse = 82;

		lfMinRangeTotalDoctor = invParam.lfGetOperationRoomDoctorNumMinWeight()*invParam.iGetOperationRoomTotalDoctorNum();
		lfMaxRangeTotalDoctor = invParam.lfGetOperationRoomDoctorNumMaxWeight()*invParam.iGetOperationRoomTotalDoctorNum();
		lfMinRangeTotalNurse = invParam.lfGetOperationRoomNurseNumMinWeight()*invParam.iGetOperationRoomTotalNurseNum();
		lfMaxRangeTotalNurse = invParam.lfGetOperationRoomNurseNumMaxWeight()*invParam.iGetOperationRoomTotalNurseNum();
		lfMinRangeRoom = invParam.iGetOperationRoomNumMin();
		lfMaxRangeRoom = invParam.iGetOperationRoomNumMax();
		lfMinRangeDoctor = invParam.iGetOperationRoomDoctorNumMin();
		lfMaxRangeDoctor = invParam.iGetOperationRoomDoctorNumMax();
		lfMinRangeNurse = invParam.iGetOperationRoomNurseNumMin();
		lfMaxRangeNurse = invParam.iGetOperationRoomNurseNumMax();

		// 手術室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[1] = lfLimitter( plfArg[1], lfMaxRangeRoom, lfMinRangeRoom );
		// 手術室の医師数を設定します。
		// 最大数を設定できるようにします。
		plfArg[16] = lfLimitter( plfArg[16], lfMaxRangeNurse, lfMinRangeNurse );
		// 手術室の看護師数を設定します。
		// 最大数を設定できるようにします。
		plfArg[17] = lfLimitter( plfArg[17], lfMaxRangeNurse, lfMinRangeNurse );

		bRet = bUpdateRoomAgent( plfArg, 1, 16, 17, lfMinRangeTotalDoctor, lfMaxRangeTotalDoctor, lfMinRangeTotalNurse, lfMaxRangeTotalNurse,
				lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeDoctor, lfMaxRangeDoctor, lfMinRangeNurse, lfMaxRangeNurse );
//		if( bRet == true ) System.out.println("手術室OK");
//		else			   System.out.println("手術室NG");

	// 初療室の制約条件を設定します。
		// 初療室の医師数
		// 初療室の看護師数
//		lfMinRangeTotalDoctor = lfMinWeight*31;
//		lfMaxRangeTotalDoctor = lfMaxWeight*31;
//		lfMinRangeTotalNurse = lfMinWeight*42;
//		lfMaxRangeTotalNurse = lfMaxWeight*42;
//		lfMinRangeRoom = 2;
//		lfMaxRangeRoom = 14;
//		lfMinRangeDoctor = 1;
//		lfMaxRangeDoctor = 15;
//		lfMinRangeNurse = 1;
//		lfMaxRangeNurse = 42;

		lfMinRangeTotalDoctor = invParam.lfGetEmergencyRoomDoctorNumMinWeight()*invParam.iGetEmergencyRoomTotalDoctorNum();
		lfMaxRangeTotalDoctor = invParam.lfGetEmergencyRoomDoctorNumMaxWeight()*invParam.iGetEmergencyRoomTotalDoctorNum();
		lfMinRangeTotalNurse = invParam.lfGetEmergencyRoomNurseNumMinWeight()*invParam.iGetEmergencyRoomTotalNurseNum();
		lfMaxRangeTotalNurse = invParam.lfGetEmergencyRoomNurseNumMaxWeight()*invParam.iGetEmergencyRoomTotalNurseNum();
		lfMinRangeRoom = invParam.iGetEmergencyRoomNumMin();
		lfMaxRangeRoom = invParam.iGetEmergencyRoomNumMax();
		lfMinRangeDoctor = invParam.iGetEmergencyRoomDoctorNumMin();
		lfMaxRangeDoctor = invParam.iGetEmergencyRoomDoctorNumMax();
		lfMinRangeNurse = invParam.iGetEmergencyRoomNurseNumMin();
		lfMaxRangeNurse = invParam.iGetEmergencyRoomNurseNumMax();

		// 初療室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[2] = lfLimitter( plfArg[2], lfMaxRangeRoom, lfMinRangeRoom );
		// 初療室の医師数を設定します。
		// 現実的に考えて1部屋に1人が担当医だと考えられるので。2人は多く見積もった。
		plfArg[18] = lfLimitter( plfArg[18], lfMaxRangeDoctor, lfMinRangeDoctor );
		// 各手術室、各初療室の看護師数は最大で4人くらいと設定します。
		// 公刊文献を調べて3例ほどから。手術室が1部屋当りの人数を算出することができたのでそれを参考に設定。
		plfArg[19] = lfLimitter( plfArg[19], lfMaxRangeNurse, lfMinRangeNurse );

		bRet = bUpdateRoomAgent( plfArg, 2, 18, 19, lfMinRangeTotalDoctor, lfMaxRangeTotalDoctor, lfMinRangeTotalNurse, lfMaxRangeTotalNurse,
				lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeDoctor, lfMaxRangeDoctor, lfMinRangeNurse, lfMaxRangeNurse );
//		if( bRet == true ) System.out.println("初療室OK");
//		else			   System.out.println("初療室NG");

		// 初療室の医療技師数
		if( plfArg[20] < 1.0 ) plfArg[20] = 1.0;
		if( plfArg[20] > 3.0 ) plfArg[20] = 3.0;

	// 観察室の制約条件を設定します。
		// 観察室の看護師数
//		lfMinRangeTotalNurse = lfMinWeight*25;
//		lfMaxRangeTotalNurse = lfMaxWeight*25;
//		lfMinRangeRoom = 0;
//		lfMaxRangeRoom = 14;
//		lfMinRangeNurse = 1;
//		lfMaxRangeNurse = 25;

		lfMinRangeTotalNurse = invParam.lfGetObservationRoomNurseNumMinWeight()*invParam.iGetObservationRoomTotalNurseNum();
		lfMaxRangeTotalNurse = invParam.lfGetObservationRoomNurseNumMaxWeight()*invParam.iGetObservationRoomTotalNurseNum();
		lfMinRangeRoom = invParam.iGetObservationRoomNumMin();
		lfMaxRangeRoom = invParam.iGetObservationRoomNumMax();
		lfMinRangeNurse = invParam.iGetObservationRoomNurseNumMin();
		lfMaxRangeNurse = invParam.iGetObservationRoomNurseNumMax();

		// 観察室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[3] = lfLimitter( plfArg[3], lfMaxRangeRoom, lfMinRangeRoom );
		// 観察室に所属する看護師は2人以下とします。
		// 公刊文献で1例だけではあるが、記載されており、算出も可能であったのでそれをベースに設定。
		plfArg[21] = lfLimitter( plfArg[21], lfMaxRangeNurse, lfMinRangeNurse );

		bRet = bUpdateNurseRelativeRoom( plfArg, 3, 21, lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeNurse, lfMaxRangeNurse,
				lfMinRangeTotalNurse, lfMaxRangeTotalNurse );

		if( bRet == true )
		{
			// 観察室の部屋数が0の場合は看護師も割り当てがないことから0とします。
			plfArg[3] = plfArg[21] == 0.0 ? 0.0 : plfArg[3];
		}
//		if( bRet == true ) System.out.println("観察室OK");
//		else			   System.out.println("観察室NG");

	// 重症観察室の制約条件を設定します。
		// 重症観察室の看護師数
//		lfMinRangeTotalNurse = lfMinWeight*25;
//		lfMaxRangeTotalNurse = lfMaxWeight*25;
//		lfMinRangeRoom = 0;
//		lfMaxRangeRoom = 4;
//		lfMinRangeNurse = 1;
//		lfMaxRangeNurse = 25;

		lfMinRangeTotalNurse = invParam.lfGetSevereInjuryObservationRoomNurseNumMinWeight()*invParam.iGetSevereInjuryObservationRoomTotalNurseNum();
		lfMaxRangeTotalNurse = invParam.lfGetSevereInjuryObservationRoomNurseNumMaxWeight()*invParam.iGetSevereInjuryObservationRoomTotalNurseNum();
		lfMinRangeRoom = invParam.iGetSevereInjuryObservationRoomNumMin();
		lfMaxRangeRoom = invParam.iGetSevereInjuryObservationRoomNumMax();
		lfMinRangeNurse = invParam.iGetSevereInjuryObservationRoomNurseNumMin();
		lfMaxRangeNurse = invParam.iGetSevereInjuryObservationRoomNurseNumMax();

		// 重症観察室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[4] = lfLimitter( plfArg[4], lfMaxRangeRoom, lfMinRangeRoom );
		// 重症観察室に所属する看護師は2人以下とします。
		// 公刊文献で1例だけではあるが、記載されており、算出も可能であったのでそれをベースに設定。
		plfArg[22] = lfLimitter( plfArg[22], lfMaxRangeNurse, lfMinRangeNurse );

		bRet = bUpdateNurseRelativeRoom( plfArg, 4, 22, lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeNurse, lfMaxRangeNurse,
				lfMinRangeTotalNurse, lfMaxRangeTotalNurse );

		if( bRet == true )
		{
			// 重症観察室の部屋数が0の場合は看護師も割り当てがないことから0とします。
			plfArg[4] = plfArg[22] == 0.0 ? 0.0 : plfArg[4];
		}
//		if( bRet == true ) System.out.println("重症観察室OK");
//		else			   System.out.println("重症観察室NG");

	// ICUの制約条件を設定します。
		// ICUの医師数
		// ICUの看護師数

//		lfMinRangeTotalDoctor = lfMinWeight*22;
//		lfMaxRangeTotalDoctor = lfMaxWeight*22;
//		lfMinRangeTotalNurse = lfMinWeight*152;
//		lfMaxRangeTotalNurse = lfMaxWeight*152;
//		lfMinRangeRoom = 4;
//		lfMaxRangeRoom = 76;
//		lfMinRangeDoctor = 1;
//		lfMaxRangeDoctor = 76;
//		lfMinRangeNurse = 1;
//		lfMaxRangeNurse = 76;

		lfMinRangeTotalDoctor = invParam.lfGetIntensiveCareUnitDoctorNumMinWeight()*invParam.iGetIntensiveCareUnitTotalDoctorNum();
		lfMaxRangeTotalDoctor = invParam.lfGetIntensiveCareUnitDoctorNumMaxWeight()*invParam.iGetIntensiveCareUnitTotalDoctorNum();
		lfMinRangeTotalNurse = invParam.lfGetIntensiveCareUnitNurseNumMinWeight()*invParam.iGetIntensiveCareUnitTotalNurseNum();
		lfMaxRangeTotalNurse = invParam.lfGetIntensiveCareUnitNurseNumMaxWeight()*invParam.iGetIntensiveCareUnitTotalNurseNum();
		lfMinRangeRoom = invParam.iGetIntensiveCareUnitNumMin();
		lfMaxRangeRoom = invParam.iGetIntensiveCareUnitNumMax();
		lfMinRangeDoctor = invParam.iGetIntensiveCareUnitDoctorNumMin();
		lfMaxRangeDoctor = invParam.iGetIntensiveCareUnitDoctorNumMax();
		lfMinRangeNurse = invParam.iGetIntensiveCareUnitNurseNumMin();
		lfMaxRangeNurse = invParam.iGetIntensiveCareUnitNurseNumMax();

		// ICU室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[5] = lfLimitter( plfArg[5], lfMaxRangeRoom, lfMinRangeRoom );
		// ICUの医師数を設定します。
		// 最大数を設定できるようにします。
		plfArg[23] = lfLimitter( plfArg[23], lfMaxRangeDoctor, lfMinRangeDoctor );
		// ICUの1人当り看護師数を設定します。
		// ソースは医療法の医療点数に記載及び公刊文献を基に設定。
		plfArg[24] = lfLimitter( plfArg[24], lfMaxRangeNurse, lfMinRangeNurse );

		bRet = bUpdateRoomAgent( plfArg, 5, 23, 24, lfMinRangeTotalDoctor, lfMaxRangeTotalDoctor, lfMinRangeTotalNurse, lfMaxRangeTotalNurse,
				lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeDoctor, lfMaxRangeDoctor, lfMinRangeNurse, lfMaxRangeNurse );
//		if( bRet == true ) System.out.println("ICU OK");
//		else			   System.out.println("ICU NG");

	// HCUの制約条件を設定します。
		// HCUの医師数
		// HCUの看護師数
//		lfMinRangeTotalDoctor = lfMinWeight*8;
//		lfMaxRangeTotalDoctor = lfMaxWeight*8;
//		lfMinRangeTotalNurse = lfMinWeight*96;
//		lfMaxRangeTotalNurse = lfMaxWeight*96;
//		lfMinRangeRoom = 0;
//		lfMaxRangeRoom = 24;
//		lfMinRangeDoctor = 1;
//		lfMaxRangeDoctor = 5;
//		lfMinRangeNurse = 1;
//		lfMaxRangeNurse = 96;

		lfMinRangeTotalDoctor = invParam.lfGetHighCareUnitDoctorNumMinWeight()*invParam.iGetHighCareUnitTotalDoctorNum();
		lfMaxRangeTotalDoctor = invParam.lfGetHighCareUnitDoctorNumMaxWeight()*invParam.iGetHighCareUnitTotalDoctorNum();
		lfMinRangeTotalNurse = invParam.lfGetHighCareUnitNurseNumMinWeight()*invParam.iGetHighCareUnitTotalNurseNum();
		lfMaxRangeTotalNurse = invParam.lfGetHighCareUnitNurseNumMaxWeight()*invParam.iGetHighCareUnitTotalNurseNum();
		lfMinRangeRoom = invParam.iGetHighCareUnitNumMin();
		lfMaxRangeRoom = invParam.iGetHighCareUnitNumMax();
		lfMinRangeDoctor = invParam.iGetHighCareUnitDoctorNumMin();
		lfMaxRangeDoctor = invParam.iGetHighCareUnitDoctorNumMax();
		lfMinRangeNurse = invParam.iGetHighCareUnitNurseNumMin();
		lfMaxRangeNurse = invParam.iGetHighCareUnitNurseNumMax();

		// HCU室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[6] = lfLimitter( plfArg[6], lfMaxRangeRoom, lfMinRangeRoom );
		// HCUの医師数を設定します。(HCUが存在している場合に設定します。)
		// 公刊文献上記載データから設定。
		plfArg[25] = lfLimitter( plfArg[25], lfMaxRangeDoctor, lfMinRangeDoctor );
		// HCUの1人当り看護師数を設定します。
		// ソースは医療法の医療点数に記載及び公刊文献を基に設定。
		plfArg[26] = lfLimitter( plfArg[26], lfMaxRangeNurse, lfMinRangeNurse );

		bRet = bUpdateRoomAgent( plfArg, 6, 25, 26, lfMinRangeTotalDoctor, lfMaxRangeTotalDoctor, lfMinRangeTotalNurse, lfMaxRangeTotalNurse,
				lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeDoctor, lfMaxRangeDoctor, lfMinRangeNurse, lfMaxRangeNurse );
		if( bRet == true )
		{
			// HCUの部屋数が0の場合は医師も割り当てがないことから0とします。
			// HCUの部屋数が0の場合は看護師も割り当てがないことから0とします。
			plfArg[25] = plfArg[6] == 0.0 ? 0.0 : plfArg[25];
			plfArg[26] = plfArg[6] == 0.0 ? 0.0 : plfArg[26];
		}
//		if( bRet == true ) System.out.println("HCU OK");
//		else			   System.out.println("HCU NG");

	// 一般病棟は制限なしとします。

//		 一般病棟の医師数を設定します。1病床に対して1人とします。
//		 実際には2人の固定値としています。
//		if( plfArg[27] < 1.0 ) plfArg[27] = 1.0;
//		if( plfArg[27] > 2.0 ) plfArg[27] = 2.0;

		// 一般病棟の看護師数
//		lfMinRangeTotalNurse = lfMinWeight*1316;
//		lfMaxRangeTotalNurse = lfMaxWeight*1316;
//		lfMinRangeRoom = 1;
//		lfMaxRangeRoom = 1296;
//		lfMinRangeNurse = 1;
//		lfMaxRangeNurse = 1296;

		lfMinRangeTotalDoctor = invParam.lfGetGeneralWardDoctorNumMinWeight()*invParam.iGetGeneralWardTotalDoctorNum();
		lfMaxRangeTotalDoctor = invParam.lfGetGeneralWardDoctorNumMaxWeight()*invParam.iGetGeneralWardTotalDoctorNum();
		lfMinRangeTotalNurse = invParam.lfGetGeneralWardNurseNumMinWeight()*invParam.iGetGeneralWardTotalNurseNum();
		lfMaxRangeTotalNurse = invParam.lfGetGeneralWardNurseNumMaxWeight()*invParam.iGetGeneralWardTotalNurseNum();
		lfMinRangeRoom = invParam.iGetGeneralWardNumMin();
		lfMaxRangeRoom = invParam.iGetGeneralWardNumMax();
		lfMinRangeDoctor = invParam.iGetGeneralWardDoctorNumMin();
		lfMaxRangeDoctor = invParam.iGetGeneralWardDoctorNumMax();
		lfMinRangeNurse = invParam.iGetGeneralWardNurseNumMin();
		lfMaxRangeNurse = invParam.iGetGeneralWardNurseNumMax();

		// 一般病棟の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[7] = lfLimitter( plfArg[7], lfMaxRangeRoom, lfMinRangeRoom );
		// 一般病棟の医師数を設定します。(実質1人のみ)、判定用にいるエージェントであり、シミュレーションにほぼ影響はない。
		plfArg[27] = lfLimitter( plfArg[27], lfMaxRangeDoctor, lfMinRangeDoctor );
		// 一般病棟の看護師数を設定します。入院患者1(1病床)に対して最大看護師7人の設定とします。
		// ソースは医療法の医療点数に記載。
		plfArg[28] = lfLimitter( plfArg[28], lfMaxRangeNurse, lfMinRangeNurse );

		bRet = bUpdateRoomAgent( plfArg, 7, 27, 28, lfMinRangeTotalDoctor, lfMaxRangeTotalDoctor, lfMinRangeTotalNurse, lfMaxRangeTotalNurse,
				lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeDoctor, lfMaxRangeDoctor, lfMinRangeNurse, lfMaxRangeNurse );
//		bRet = bUpdateNurseRelativeRoom( plfArg, 7, 28, lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeNurse, lfMaxRangeNurse,
//				lfMinRangeTotalNurse, lfMaxRangeTotalNurse );
//		if( bRet == true ) System.out.println("一般病棟OK");
//		else			   System.out.println("一般病棟NG");

	// 待合室の制約条件を設定します。

		lfMinRangeTotalNurse = invParam.lfGetWaitingRoomNurseNumMinWeight()*invParam.iGetWaitingRoomTotalNurseNum();
		lfMaxRangeTotalNurse = invParam.lfGetWaitingRoomNurseNumMaxWeight()*invParam.iGetWaitingRoomTotalNurseNum();
		lfMinRangeRoom = invParam.iGetWaitingRoomNumMin();
		lfMaxRangeRoom = invParam.iGetWaitingRoomNumMax();
		lfMinRangeNurse = invParam.iGetWaitingRoomNurseNumMin();
		lfMaxRangeNurse = invParam.iGetWaitingRoomNurseNumMax();

		// 待合室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[8] = lfLimitter( plfArg[8], lfMaxRangeRoom, lfMinRangeRoom );
		// 待合室に所属する看護師は2人以下とします。
		// 公刊文献で1例だけではあるが、記載されており、算出も可能であったのでそれをベースに設定。
		plfArg[29] = lfLimitter( plfArg[29], lfMaxRangeNurse, lfMinRangeNurse );

		bRet = bUpdateNurseRelativeRoom( plfArg, 8, 29, lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeNurse, lfMaxRangeNurse,
				lfMinRangeTotalNurse, lfMaxRangeTotalNurse );

		// 待合室は1部屋とします。
//		plfArg[8] = 1;
		// 待合室の看護師の数を設定します。
		// 1人以上はいると予想されるので、そのように設定します。
		// 入院患者1(1病床)に対して最大看護師7人の設定とします。(医療法の医療点数を基に設定しました。)
//		if( plfArg[29] < 1.0 ) plfArg[29] = 1.0;
//		if( plfArg[29] > 7.0 ) plfArg[29] = 7.0;

	// X線室の制約条件を設定します。
		// 総数に関しても同様に調査結果を基に設定。
//		lfMinRangeTotalClinicalEngineer = lfMinWeight*8;
//		lfMaxRangeTotalClinicalEngineer = lfMaxWeight*8;
//		lfMinRangeRoom = 1;
//		lfMaxRangeRoom = 18;
//		lfMinRangeClinicalEngineer = 1;
//		lfMaxRangeClinicalEngineer = 1;

		lfMinRangeTotalClinicalEngineer = invParam.lfGetXRayRoomClinicalEngineerNumMinWeight()*invParam.iGetXRayRoomTotalClinicalEngineerNum();
		lfMaxRangeTotalClinicalEngineer = invParam.lfGetXRayRoomClinicalEngineerNumMaxWeight()*invParam.iGetXRayRoomTotalClinicalEngineerNum();
		lfMinRangeRoom = invParam.iGetXRayRoomNumMin();
		lfMaxRangeRoom = invParam.iGetXRayRoomNumMax();
		lfMinRangeClinicalEngineer = invParam.iGetXRayRoomClinicalEngineerNumMin();
		lfMaxRangeClinicalEngineer = invParam.iGetXRayRoomClinicalEngineerNumMax();

		// X線室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[9] = lfLimitter( plfArg[9], lfMaxRangeRoom, lfMinRangeRoom );
		// 各検査室に所属する医療技師数は1人以上3人以下とします。
		// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
		// 公刊文献にも詳しい値が記載されていないことから。
		plfArg[30] = lfLimitter( plfArg[30], lfMaxRangeClinicalEngineer, lfMinRangeClinicalEngineer );

		bRet = bUpdateClinicalEngineerRelativeRoom( plfArg, 9, 30,
				lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeClinicalEngineer, lfMaxRangeClinicalEngineer,
				lfMinRangeTotalClinicalEngineer, lfMaxRangeTotalClinicalEngineer );
//		if( bRet == true ) System.out.println("X線室OK");
//		else			   System.out.println("X線室NG");
		if( plfArg[9] > lfMaxRangeRoom )	System.out.println("制約条件を超える事象あり。X線室。");
		if( plfArg[30] > lfMaxRangeClinicalEngineer )	System.out.println("制約条件を超える事象あり。X線室医療技師。");


	// CT室の制約条件を設定します。
//		lfMinRangeTotalClinicalEngineer = lfMinWeight*4;
//		lfMaxRangeTotalClinicalEngineer = lfMaxWeight*4;
//		lfMinRangeRoom = 1;
//		lfMaxRangeRoom = 4;
//		lfMaxRangeRoom = 16;
//		lfMinRangeClinicalEngineer = 1;
//		lfMaxRangeClinicalEngineer = 1;

		lfMinRangeTotalClinicalEngineer = invParam.lfGetCTRoomClinicalEngineerNumMinWeight()*invParam.iGetCTRoomTotalClinicalEngineerNum();
		lfMaxRangeTotalClinicalEngineer = invParam.lfGetCTRoomClinicalEngineerNumMaxWeight()*invParam.iGetCTRoomTotalClinicalEngineerNum();
		lfMinRangeRoom = invParam.iGetCTRoomNumMin();
		lfMaxRangeRoom = invParam.iGetCTRoomNumMax();
		lfMinRangeClinicalEngineer = invParam.iGetCTRoomClinicalEngineerNumMin();
		lfMaxRangeClinicalEngineer = invParam.iGetCTRoomClinicalEngineerNumMax();

		// CT室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[10] = lfLimitter( plfArg[10], lfMaxRangeRoom, lfMinRangeRoom );
		// 各検査室に所属する医療技師数は1人以上3人以下とします。
		// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
		// 公刊文献にも詳しい値が記載されていないことから。
		plfArg[31] = lfLimitter( plfArg[31], lfMaxRangeClinicalEngineer, lfMinRangeClinicalEngineer );

		bRet = bUpdateClinicalEngineerRelativeRoom( plfArg, 10, 31,
				lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeClinicalEngineer, lfMaxRangeClinicalEngineer,
				lfMinRangeTotalClinicalEngineer, lfMaxRangeTotalClinicalEngineer );
		if( plfArg[10] > lfMaxRangeRoom )	System.out.println("制約条件を超える事象あり。CT室。");
		if( plfArg[31] > lfMaxRangeClinicalEngineer )	System.out.println("制約条件を超える事象あり。CT室医療技師。");

	// MRI室の制約条件を設定します。
//		lfMinRangeTotalClinicalEngineer = lfMinWeight*3;
//		lfMaxRangeTotalClinicalEngineer = lfMaxWeight*3;
//		lfMinRangeRoom = 1;
//		lfMaxRangeRoom = 7;
//		lfMinRangeClinicalEngineer = 1;
//		lfMaxRangeClinicalEngineer = 1;

		lfMinRangeTotalClinicalEngineer = invParam.lfGetMRIRoomClinicalEngineerNumMinWeight()*invParam.iGetMRIRoomTotalClinicalEngineerNum();
		lfMaxRangeTotalClinicalEngineer = invParam.lfGetMRIRoomClinicalEngineerNumMaxWeight()*invParam.iGetMRIRoomTotalClinicalEngineerNum();
		lfMinRangeRoom = invParam.iGetMRIRoomNumMin();
		lfMaxRangeRoom = invParam.iGetMRIRoomNumMax();
		lfMinRangeClinicalEngineer = invParam.iGetMRIRoomClinicalEngineerNumMin();
		lfMaxRangeClinicalEngineer = invParam.iGetMRIRoomClinicalEngineerNumMax();

		// MRI室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[11] = lfLimitter( plfArg[11], lfMaxRangeRoom, lfMinRangeRoom );
		// 各検査室に所属する医療技師数は1人以上3人以下とします。
		// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
		// 公刊文献にも詳しい値が記載されていないことから。
		plfArg[32] = lfLimitter( plfArg[32], lfMaxRangeClinicalEngineer, lfMinRangeClinicalEngineer );

		bRet = bUpdateClinicalEngineerRelativeRoom( plfArg, 11, 32,
				lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeClinicalEngineer, lfMaxRangeClinicalEngineer,
				lfMinRangeTotalClinicalEngineer, lfMaxRangeTotalClinicalEngineer );
//		if( bRet == true ) System.out.println("MRI室OK");
//		else			   System.out.println("MRI室NG");
		if( plfArg[11] > lfMaxRangeRoom )	System.out.println("制約条件を超える事象あり。MRI室。");
		if( plfArg[32] > lfMaxRangeClinicalEngineer )	System.out.println("制約条件を超える事象あり。MRI医療技師。");

	// 血管造影室の制約条件を設定します。
//		lfMinRangeTotalClinicalEngineer = lfMinWeight*4;
//		lfMaxRangeTotalClinicalEngineer = lfMaxWeight*4;
//		lfMinRangeRoom = 1;
//		lfMaxRangeRoom = 9;
//		lfMinRangeClinicalEngineer = 1;
//		lfMaxRangeClinicalEngineer = 1;

		lfMinRangeTotalClinicalEngineer = invParam.lfGetAngiographyRoomClinicalEngineerNumMinWeight()*invParam.iGetAngiographyRoomTotalClinicalEngineerNum();
		lfMaxRangeTotalClinicalEngineer = invParam.lfGetAngiographyRoomClinicalEngineerNumMaxWeight()*invParam.iGetAngiographyRoomTotalClinicalEngineerNum();
		lfMinRangeRoom = invParam.iGetAngiographyRoomNumMin();
		lfMaxRangeRoom = invParam.iGetAngiographyRoomNumMax();
		lfMinRangeClinicalEngineer = invParam.iGetAngiographyRoomClinicalEngineerNumMin();
		lfMaxRangeClinicalEngineer = invParam.iGetAngiographyRoomClinicalEngineerNumMax();

		// 血管造影室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[12] = lfLimitter( plfArg[12], lfMaxRangeRoom, lfMinRangeRoom );
		// 各検査室に所属する医療技師数は1人以上3人以下とします。
		// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
		// 公刊文献にも詳しい値が記載されていないことから。
		plfArg[33] = lfLimitter( plfArg[33], lfMaxRangeClinicalEngineer, lfMinRangeClinicalEngineer );

		bRet = bUpdateClinicalEngineerRelativeRoom( plfArg, 12, 33,
				lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeClinicalEngineer, lfMaxRangeClinicalEngineer,
				lfMinRangeTotalClinicalEngineer, lfMaxRangeTotalClinicalEngineer );
//		if( bRet == true ) System.out.println("血管造影室OK");
//		else			   System.out.println("血管造影室NG");
		if( plfArg[12] > lfMaxRangeRoom )	System.out.println("制約条件を超える事象あり。血管造影室。");
		if( plfArg[33] > lfMaxRangeClinicalEngineer )	System.out.println("制約条件を超える事象あり。血管造影室医療技師。");

	// FAST室の制約条件を設定します。
//		lfMinRangeTotalClinicalEngineer = lfMinWeight*5;
//		lfMaxRangeTotalClinicalEngineer = lfMaxWeight*5;
//		lfMinRangeRoom = 1;
//		lfMaxRangeRoom = 12;
//		lfMinRangeClinicalEngineer = 1;
//		lfMaxRangeClinicalEngineer = 1;

		lfMinRangeTotalClinicalEngineer = invParam.lfGetFastRoomClinicalEngineerNumMinWeight()*invParam.iGetFastRoomTotalClinicalEngineerNum();
		lfMaxRangeTotalClinicalEngineer = invParam.lfGetFastRoomClinicalEngineerNumMaxWeight()*invParam.iGetFastRoomTotalClinicalEngineerNum();
		lfMinRangeRoom = invParam.iGetFastRoomNumMin();
		lfMaxRangeRoom = invParam.iGetFastRoomNumMax();
		lfMinRangeClinicalEngineer = invParam.iGetFastRoomClinicalEngineerNumMin();
		lfMaxRangeClinicalEngineer = invParam.iGetFastRoomClinicalEngineerNumMax();

		// 超音波室の部屋数を設定します。
		// 調査結果を基に設定。
		plfArg[13] = lfLimitter( plfArg[13], lfMaxRangeRoom, lfMinRangeRoom );
		// 各検査室に所属する医療技師数は1人以上3人以下とします。
		// ソースは医療法には適当数のみ。なので、1部屋あたり3人程度とします。
		// 公刊文献にも詳しい値が記載されていないことから。
		plfArg[34] = lfLimitter( plfArg[34], lfMaxRangeClinicalEngineer, lfMinRangeClinicalEngineer );

		bRet = bUpdateClinicalEngineerRelativeRoom( plfArg, 13, 34,
				lfMinRangeRoom, lfMaxRangeRoom, lfMinRangeClinicalEngineer, lfMaxRangeClinicalEngineer,
				lfMinRangeTotalClinicalEngineer, lfMaxRangeTotalClinicalEngineer );
//		if( bRet == true ) System.out.println("FAST室OK");
//		else			   System.out.println("FAST室NG");
		/* デバッグ用 */
		if( plfArg[13] > lfMaxRangeRoom )	System.out.println("制約条件を超える事象あり。超音波室。");
		if( plfArg[34] > lfMaxRangeClinicalEngineer )	System.out.println("制約条件を超える事象あり。超音波室医療技師。");

	// 部屋を構成するエージェント数の設定

		// 年間で来院する患者の人数の算出が必要です。
		// 筑波メディカルセンターの場合は・・・
		// 聖隷浜松病院の場合は・・・19379人
		// 各診察室の医師数は最大でも2人くらいに設定します。
		// 算出方法は医療法に記載のものから算出。
//		// 患者の総数を取得します。
//		lfPatientsNum = 19379;
//		// 必要医師数を算出します。
//		lfDoctorsNum = lfPatientsNum*0.0625+lfPatientsNum*1.0/16.0;
//		// 必要看護師数を算出します。
//		lfNursesNum = lfPatientsNum*1.0/3.0+lfPatientsNum*1.0/30.0;
//		// 総部屋数を算出します。
//		lfTotalRoomNum = plfArg[0]+plfArg[1]+plfArg[2]+plfArg[3]+plfArg[4]+plfArg[5]+plfArg[6]+plfArg[7]+plfArg[8]+plfArg[9]+plfArg[10]+plfArg[11]+plfArg[12]+plfArg[13];
//
//		lfOneRoomDoctorNum			 = lfDoctorsNum / lfTotalRoomNum;
//		lfOneRoomNurseNum			 = lfNursesNum / lfTotalRoomNum;
//		lfOneRoomClinicalEngineerNum = lfClinicalEngineersNum / lfTotalRoomNum;

	}

	/**
	 * <PRE>
	 *  リミッターをかけます。
	 * </PRE>
	 * @param lfData	リミッターをかけるデータ
	 * @param lfMax		リミッターの最大値
	 * @param lfMin		リミッターの最小値
	 * @return	リミッターをかけた結果
	 */
	private double lfLimitter( double lfData, double lfMax, double lfMin )
	{
		double lfRes;
		lfRes = lfMin > lfData ? lfMin : lfData;
		lfRes = lfMax < lfRes ? lfMax : lfRes;
		return lfRes;
	}

	/**
	 * <PRE>
	 * 	看護師のみの場合の制約条件をかける関数。
	 *  なるべく現状の値に最も近いデータを採用するように設定。
	 *  ユークリッド距離を使用。
	 * </PRE>
	 * @param plfArg				代入変数
	 * @param iRoomLoc				現在選択している変数の番号（部屋）
	 * @param iNurseLoc				現在選択している変数の番号（看護師）
	 * @param lfMinRangeRoom		部屋数の最小値
	 * @param lfMaxRangeRoom		部屋数の最大値
	 * @param lfMinRangeNurse		1部屋当りの看護師の最小値
	 * @param lfMaxRangeNurse		1部屋当りの看護師の最大値
	 * @param lfMinRangeTotalNurse	看護師総数の最小値
	 * @param lfMaxRangeTotalNurse	看護師総数の最大値
	 * @return	true	値変更完了
	 * 			false 	値変更せず
	 */
	private boolean bUpdateNurseRelativeRoom( double[] plfArg, int iRoomLoc, int iNurseLoc,
			double lfMinRangeRoom, double lfMaxRangeRoom, double lfMinRangeNurse, double lfMaxRangeNurse,
			double lfMinRangeTotalNurse, double lfMaxRangeTotalNurse )
	{
		double lfRoom, lfNurse, lfMinRoom, lfMinNurse;
		double lfDistance, lfMinDistance;
		boolean bRet = false;

		lfMinRoom = lfMinNurse = Double.MAX_VALUE;
		lfMinDistance = Double.MAX_VALUE;

		// 設定上看護師が割り当てられないときは0を代入して終了します。
		if( lfMinRangeTotalNurse == 0.0 && lfMaxRangeTotalNurse == 0.0 )
		{
			plfArg[iRoomLoc] = 0.0;
			plfArg[iNurseLoc] = 0.0;
			return true;
		}
		// 部屋数を順にみていく。
		for( lfRoom = lfMinRangeRoom; lfRoom <= lfMaxRangeRoom; lfRoom += 1.0 )
		{
			if( lfRoom == 0.0 ) continue;
			// 看護師数を順にみていく。
			for( lfNurse = lfMinRangeNurse; lfNurse <= lfMaxRangeNurse; lfNurse += 1.0 )
			{
				if( lfNurse == 0.0 ) continue;
				// 看護師総数が条件を満たすかどうかを判定します。
				if( lfMinRangeTotalNurse <= lfRoom*lfNurse && lfRoom*lfNurse <= lfMaxRangeTotalNurse )
				{
					// 判定後、どの値が適切な値かを判定します。距離尺度により判定します。
					// 現在の値に最も近いベクトル適切な値としてを採用します。ベクトル計算とみなし、ユークリッド距離により評価を実施。
					lfDistance = Math.sqrt( (lfRoom - plfArg[iRoomLoc])*(lfRoom - plfArg[iRoomLoc])+(lfNurse - plfArg[iNurseLoc])*(lfNurse - plfArg[iNurseLoc] ));
					if( lfDistance <= lfMinDistance )
					{
						// そのときの部屋数、医師数、看護師数を取得します。
						lfMinRoom = lfRoom;
						lfMinNurse = lfNurse;
						lfMinDistance = lfDistance;
						bRet = true;
					}
				}
			}
		}
		// 該当データの組み合わせが見つかった場合
		if( bRet == true )
		{
			plfArg[iRoomLoc] = lfMinRoom;
			plfArg[iNurseLoc] = lfMinNurse;
		}
		return bRet;
	}

	/**
	 * <PRE>
	 * 	医療技師のみの場合の制約条件をかける関数。
	 *  なるべく現状の値に最も近いデータを採用するように設定。
	 *  ユークリッド距離を使用。
	 * </PRE>
	 * @param plfArg							代入変数
	 * @param iRoomLoc							現在選択している変数の番号（部屋）
	 * @param iClinicalEngineerLoc				現在選択している変数の番号（医療技師）
	 * @param lfMinRangeRoom					部屋数の最小値
	 * @param lfMaxRangeRoom					部屋数の最大値
	 * @param lfMinRangeClinicalEngineer		1部屋当りの医療技師最小値
	 * @param lfMaxRangeClinicalEngineer		1部屋当りの医療技師最大値
	 * @param lfMinRangeTotalClinicalEngineer	医療技師総数最小値
	 * @param lfMaxRangeTotalClinicalEngineer	医療技師総数最大値
	 * @return	true	値変更完了
	 * 			false 	値変更せず
	 */
	private boolean bUpdateClinicalEngineerRelativeRoom( double[] plfArg, int iRoomLoc, int iClinicalEngineerLoc,
			double lfMinRangeRoom, double lfMaxRangeRoom, double lfMinRangeClinicalEngineer, double lfMaxRangeClinicalEngineer,
			double lfMinRangeTotalClinicalEngineer, double lfMaxRangeTotalClinicalEngineer )
	{
		double lfRoom, lfClinicalEngineer, lfMinRoom, lfMinClinicalEngineer;
		double lfDistance, lfMinDistance;
		boolean bRet = false;

		lfMinRoom = lfMinClinicalEngineer = Double.MAX_VALUE;
		lfMinDistance = Double.MAX_VALUE;

		// 設定上医療技師が割り当てられないときは0を代入して終了します。
		if( lfMinRangeTotalClinicalEngineer == 0.0 && lfMaxRangeTotalClinicalEngineer == 0.0 )
		{
			plfArg[iRoomLoc] = 0.0;
			plfArg[iClinicalEngineerLoc] = 0.0;
			return true;
		}
		// 部屋数を順にみていく。
		for( lfRoom = lfMinRangeRoom; lfRoom <= lfMaxRangeRoom; lfRoom += 1.0 )
		{
			if( lfRoom == 0.0 ) continue;
			// 医療技師数を順にみていく。
			for( lfClinicalEngineer = lfMinRangeClinicalEngineer; lfClinicalEngineer <= lfMaxRangeClinicalEngineer; lfClinicalEngineer += 1.0 )
			{
				if( lfClinicalEngineer == 0.0 ) continue;
				// 医療技師総数が条件を満たすかどうかを判定します。
				if( lfMinRangeTotalClinicalEngineer <= lfRoom*lfClinicalEngineer && lfRoom*lfClinicalEngineer <= lfMaxRangeTotalClinicalEngineer )
				{
					// 判定後、どの値が適切な値かを判定します。距離尺度により判定します。
					// 現在の値に最も近いベクトル適切な値としてを採用します。ベクトル計算とみなし、ユークリッド距離により評価を実施。
					lfDistance = Math.sqrt( (lfRoom - plfArg[iRoomLoc])*(lfRoom - plfArg[iRoomLoc])+(lfClinicalEngineer - plfArg[iClinicalEngineerLoc])*(lfClinicalEngineer - plfArg[iClinicalEngineerLoc] ));
					if( lfDistance <= lfMinDistance )
					{
						// そのときの部屋数、医師数、看護師数を取得します。
						lfMinRoom = lfRoom;
						lfMinClinicalEngineer = lfClinicalEngineer;
						lfMinDistance = lfDistance;
						bRet = true;
					}
				}
			}
		}
		// 該当データの組み合わせが見つかった場合
		if( bRet == true )
		{
			plfArg[iRoomLoc] = lfMinRoom;
			plfArg[iClinicalEngineerLoc] = lfMinClinicalEngineer;
		}
		return bRet;
	}

	/**
	 * <PRE>
	 * 	医療技師のみの場合の制約条件をかける関数。
	 *  なるべく現状の値に最も近いデータを採用するように設定。
	 *  ユークリッド距離を使用。
	 * </PRE>
	 * @param plfArg						代入変数
	 * @param iRoomLoc						現在選択している変数の番号（部屋）
	 * @param iDoctorLoc					現在選択している変数の番号（医師）
	 * @param iNurseLoc						現在選択している変数の番号（看護師）
	 * @param lfMinRangeTotalDoctor			医師総数最小値
	 * @param lfMaxRangeTotalDoctor			医師総数最大値
	 * @param lfMinRangeTotalNurse			看護師総数の最小値
	 * @param lfMaxRangeTotalNurse			看護師総数の最大値
	 * @param lfMinRangeRoom				部屋数の最小値
	 * @param lfMaxRangeRoom				部屋数の最大値
	 * @param lfMinRangeDoctor				1部屋当りの医療技師最小値
	 * @param lfMaxRangeDoctor				1部屋当りの医療技師最大値
	 * @param lfMinRangeNurse				1部屋当りの看護師の最小値
	 * @param lfMaxRangeNurse				1部屋当りの看護師の最大値
	 * @return	true	値変更完了
	 * 			false 	値変更せず
	 */
	private boolean bUpdateRoomAgent( double[] plfArg, int iRoomLoc, int iDoctorLoc, int iNurseLoc,
			double lfMinRangeTotalDoctor, double lfMaxRangeTotalDoctor, double lfMinRangeTotalNurse, double lfMaxRangeTotalNurse,
			double lfMinRangeRoom, double lfMaxRangeRoom, double lfMinRangeDoctor, double lfMaxRangeDoctor, double lfMinRangeNurse, double lfMaxRangeNurse )
	{

		double lfRoom, lfDoctor, lfNurse, lfMinRoom, lfMinDoctor, lfMinNurse;
		double lfDistance, lfMinDistance;
		boolean bRet = false;

		lfMinRoom = lfMinDoctor = lfMinNurse = Double.MAX_VALUE;
		lfMinDistance = Double.MAX_VALUE;

		// 設定上医師が割り当てられないときはすべて0を代入して終了します。
		if( lfMinRangeTotalDoctor == 0.0 && lfMaxRangeTotalDoctor == 0.0 )
		{
			plfArg[iRoomLoc] = 0.0;
			plfArg[iDoctorLoc] = 0.0;
			plfArg[iNurseLoc] = 0.0;
			return true;
		}
		// 設定上看護師が割り当てられないときはすべて0を代入して終了します。
		if( lfMinRangeTotalNurse == 0.0 && lfMaxRangeTotalNurse == 0.0 )
		{
			plfArg[iRoomLoc] = 0.0;
			plfArg[iDoctorLoc] = 0.0;
			plfArg[iNurseLoc] = 0.0;
			return true;
		}
		// 部屋数を順にみていく。
		for( lfRoom = lfMinRangeRoom; lfRoom <= lfMaxRangeRoom; lfRoom += 1.0 )
		{
			if( lfRoom == 0.0 ) continue;
			// 医師数を順にみていく。
			for( lfDoctor = lfMinRangeDoctor; lfDoctor <= lfMaxRangeDoctor; lfDoctor += 1.0 )
			{
				if( lfDoctor == 0.0 ) continue;
				// 看護師数を順にみていく。
				for( lfNurse = lfMinRangeNurse; lfNurse <= lfMaxRangeNurse; lfNurse += 1.0 )
				{
					if( lfNurse == 0.0 ) continue;
//					System.out.println(lfMinRangeTotalDoctor + "<=" + lfRoom*lfDoctor + "<=" + lfMaxRangeTotalDoctor );
//					System.out.println(lfMinRangeTotalNurse + "<=" + lfRoom*lfNurse + "<=" + lfMaxRangeTotalNurse );
					// 医師総数及び看護師総数が条件を満たすかどうかを判定します。
					if( ( lfMinRangeTotalDoctor <= lfRoom*lfDoctor && lfRoom*lfDoctor <= lfMaxRangeTotalDoctor ) &&
						( lfMinRangeTotalNurse <= lfRoom*lfNurse && lfRoom*lfNurse <= lfMaxRangeTotalNurse ) )
					{
						// 判定後、どの値が適切な値かを判定します。距離尺度により判定します。
						// 現在の値に最も近いベクトル適切な値としてを採用します。ベクトル計算とみなし、ユークリッド距離により評価を実施。
						lfDistance = Math.sqrt( (lfRoom - plfArg[iRoomLoc])*(lfRoom - plfArg[iRoomLoc])+(lfDoctor - plfArg[iDoctorLoc])*(lfDoctor - plfArg[iDoctorLoc])+(lfNurse - plfArg[iNurseLoc])*(lfNurse - plfArg[iNurseLoc]) );
						if( lfDistance <= lfMinDistance )
						{
							// そのときの部屋数、医師数、看護師数を取得します。
							lfMinRoom = lfRoom;
							lfMinDoctor = lfDoctor;
							lfMinNurse = lfNurse;
							lfMinDistance = lfDistance;
							bRet = true;
						}
					}
				}
			}
		}
		// 該当データの組み合わせが見つかった場合
		if( bRet == true )
		{
			plfArg[iRoomLoc] = lfMinRoom;
			plfArg[iDoctorLoc] = lfMinDoctor;
			plfArg[iNurseLoc] = lfMinNurse;
		}
		return bRet;
	}
}
