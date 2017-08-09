package utility.node;

import java.util.Comparator;

public class ERTriageSortNodes implements Comparator<ERTriageNode>
{
	public int compare(ERTriageNode o1, ERTriageNode o2)
	{
		long result = o1.getId() - o2.getId();
		if ( result < 0 )
		{
			return -1;
		}
		else if ( result > 0 )
		{
			return 1;
		}
		return 0;
	}
}
