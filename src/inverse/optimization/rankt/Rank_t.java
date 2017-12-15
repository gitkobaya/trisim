package inverse.optimization.rankt;

/**
 *    ��`�I�A���S���Y���y�ѐl�H�I�R���j�[�A���S���Y���Ŏg�p����
 *    �f�[�^�\�[�g�p�N���X�B
 *    ������g���ăf�[�^�\�[�g���s���܂��B
 *
 * @author kobayashi
 *
 */
public class Rank_t
{
	public int iLoc;			// �z��ԍ�
	public double lfFitProb;	// �K���x

	/**
	 * <PRE>
	 *   �f�t�H���g�R���X�g���N�^�i�������܂���B�j
	 * </PRE>
	 */
	public Rank_t()
	{

	}

	/**
	 * <PRE>
	 *    �R���X�g���N�^�i�z��ԍ��A�K���x�ŏ������j
	 * </PRE>
	 * @param iData	 �z��ԍ�
	 * @param lfData �K���x
	 */
	public Rank_t( int iData, double lfData )
	{
		iLoc = iData;
		lfFitProb = lfData;
	}
}
