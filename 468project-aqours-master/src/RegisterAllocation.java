public class RegisterAllocation
{
	public int registerNum;
	public String registers[];
	public RegisterAllocation(int registerNum)
	{
		this.registerNum = registerNum;
		registers = new String[registerNum];
		init();
	}
	//you must use init before parasing every functions block
	public void init()
	{
		for(int i=0;i<registers.length;i++)
		{
			registers[i] = null;
		}		
	}
	public void free(String v)
	{
		for(int i=0;i<registers.length;i++)
		{
			if(registers[i]!=null&&registers[i].equals(v))
			{
				registers[i] = null;
				break;
			}
		}		
	}
	public void free(int r)
	{
		registers[r] = null;
	}
	public String ensure(String v)
	{
		boolean ret = false;
		String r = null;
		for(int i=0;i<registers.length;i++)
		{
			if(registers[i]!=null&&registers[i].equals(v))
			{
				r = String.format("r%d", i);
				ret = true;
				break;
			}
		}
		if(!ret)
			r = allocate(v);
		return r;
	}
	private String allocate(String v)
	{
		String r = null;
		boolean free = false;
		for(int i=0;i<registers.length;i++)
		{
			if(registers[i]==null)
			{
				free = true;
				r = String.format("r%d", i);
				registers[i] = v;
				break;
			}
		}
		if(!free)
		{
			//no free register
			for(int i=0;i<registers.length;i++)
			{
				if(!registers[i].startsWith("$T"))
				{
					registers[i] = v;
					r = String.format("r%d", i);
				}
			}
		}
		return r;
	}
}
