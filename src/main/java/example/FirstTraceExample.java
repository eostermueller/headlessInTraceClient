package example;

import java.util.Arrays;

public class FirstTraceExample
{

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args)
  {
    try
    {
      otherMain(args[0]);
    }
    catch (Throwable ex)
    {
      ex.printStackTrace();
    }
  }

  public static void otherMain(String arg) throws Exception
  {
    while (true)
    {
    	interfaceTest();
      Thread.sleep(Long.parseLong(arg));
      workMethod("foobar");
    }
  }

  private static void workMethod(String foo)
  {
    long currentTime = System.currentTimeMillis();
    System.setProperty("a", foo);
    System.setProperty("foo", exceptionMethod());
    System.setProperty("foo", ": " + intArrayMethod(new int[]
    { 1, 2, 3 }));
    if ((currentTime % 2) == 0)
    {
      System.setProperty("a", "Even time");
    }
    else
    {
      System.setProperty("a", "Odd time");
    }
    interfaceTest();
  }

  private static void interfaceTest() {
	MyTestInterface mti = new MyFirstTestImplementor();
	mti.foo();
	
}

private static String exceptionMethod()
  {
    try
    {
      long currentTime = System.currentTimeMillis();
      if ((currentTime % 2) == 0)
      {
        throw new Exception("Exception text");
      }
    }
    catch (Exception ex)
    {
      return "seen exception";
    }
    return "no exception";
  }

  private static int intArrayMethod(int[] intArg)
  {
    System.setProperty("a", Arrays.toString(intArg));
    return 123;
  }
}
