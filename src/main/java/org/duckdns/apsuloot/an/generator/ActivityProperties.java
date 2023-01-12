package org.duckdns.apsuloot.an.generator;

public class ActivityProperties
{

	private Boolean visibility;
	private Boolean random;
	private int box;

	ActivityProperties()
	{
		this.visibility = true;
		this.random = true;
		this.box = 1;
	}

	public Boolean getVisibility()
	{
		return visibility;
	}

	public void setVisibility(Boolean visibility)
	{
		this.visibility = visibility;
	}

	public int getBox()
	{
		return box;
	}

	public void setBox(int box)
	{
		if (box < 1 || box > 3)
		{
			throw new IllegalArgumentException("Bad Configuration. Available boxes: [1, 2, 3]");
		}
		this.box = box;
	}

	public Boolean getRandom()
	{
		return random;
	}

	public void setRandom(Boolean random)
	{
		this.random = random;
	}
}
