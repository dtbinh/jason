package test;

import junit.framework.TestCase;
import env.WorldFactory;
import env.WorldModel;

public class WorldBuilderTest extends TestCase {

	public void testFence()  {
		try {
			WorldModel model = WorldFactory.worldFromContest2007("Fence");
			assertTrue(model != null);
			
			assertEquals(51,model.getWidth());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
