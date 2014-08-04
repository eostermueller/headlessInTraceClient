package example;

public class MyFirstTestImplementor implements MyTestInterface {

	@Override
	public void foo(long pause) {
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
