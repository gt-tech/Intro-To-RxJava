package itrx.chapter4.coincidence;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

public class JoinTest {
	
	private static class Tuple<T1, T2> {
		public final T1 item1;
		public final T2 item2;
		
		public Tuple(T1 item1, T2 item2) {
			this.item1 = item1;
			this.item2 = item2;
		}
		
		public static <T1,T2> Tuple<T1,T2> create(T1 item1, T2 item2) {
			return new Tuple<T1,T2>(item1, item2);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Tuple<?,?>) {
				Tuple<?,?> other = (Tuple<?,?>) obj;
				return this.item1.equals(other.item1) &&
						this.item2.equals(other.item2);
			}
			return false;				
		}
	}

	public void exampleJoinSimple() {
		Observable<String> left = 
		        Observable.interval(100, TimeUnit.MILLISECONDS)
		            .map(i -> "L" + i);
		Observable<String> right = 
		        Observable.interval(200, TimeUnit.MILLISECONDS)
		            .map(i -> "R" + i);

		left
		    .join(
		        right,
		        i -> Observable.never(),
		        i -> Observable.timer(0, TimeUnit.MILLISECONDS),
		        (l,r) -> l + " - " + r
		    )
		    .take(10)
		    .subscribe(System.out::println);
		
		// L0 - R0
		// L1 - R0
		// L0 - R1
		// L1 - R1
		// L2 - R1
		// L3 - R1
		// L0 - R2
		// L1 - R2
		// L2 - R2
		// L3 - R2
	}
	
	public void exampleJoin2Way() {
		Observable<String> left = 
		        Observable.interval(100, TimeUnit.MILLISECONDS)
		            .map(i -> "L" + i);
		Observable<String> right = 
		        Observable.interval(100, TimeUnit.MILLISECONDS)
		            .map(i -> "R" + i);

		left
		    .join(
		        right,
		        i -> Observable.timer(150, TimeUnit.MILLISECONDS),
		        i -> Observable.timer(0, TimeUnit.MILLISECONDS),
		        (l,r) -> l + " - " + r
		    )
		    .take(10)
		    .subscribe(System.out::println);
		
		// L0 - R0
		// L0 - R1
		// L1 - R1
		// L1 - R2
		// L2 - R2
		// L2 - R3
		// L3 - R3
		// L3 - R4
		// L4 - R4
		// L4 - R5
	}
	
	
	//
	// Tests
	//
	
	@Test
	public void testJoinSimple() {
		TestSubscriber<Tuple<Long,Long>> tester = new TestSubscriber<>();
		TestScheduler scheduler = Schedulers.test();
		
		Observable<Long> left = 
		        Observable.interval(100, TimeUnit.MILLISECONDS, scheduler);
		Observable<Long> right = 
		        Observable.interval(200, TimeUnit.MILLISECONDS, scheduler);

		left
		    .join(
		        right,
		        i -> Observable.never(),
		        i -> Observable.timer(0, TimeUnit.MILLISECONDS, scheduler),
		        (l,r) -> Tuple.create(l, r)
		    )
		    .take(10)
		    .subscribe(tester);
		
		scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
		tester.assertReceivedOnNext(Arrays.asList(
			Tuple.create(0L, 0L),
			Tuple.create(1L, 0L),
			Tuple.create(0L, 1L),
			Tuple.create(1L, 1L),
			Tuple.create(2L, 1L),
			Tuple.create(3L, 1L),
			Tuple.create(0L, 2L),
			Tuple.create(1L, 2L),
			Tuple.create(2L, 2L),
			Tuple.create(3L, 2L)
		));
	}
	
	@Test
	public void testJoin2Way() {
		TestSubscriber<Tuple<Long,Long>> tester = new TestSubscriber<>();
		TestScheduler scheduler = Schedulers.test();
		
		Observable<Long> left = 
		        Observable.interval(100, TimeUnit.MILLISECONDS, scheduler);
		Observable<Long> right = 
		        Observable.interval(100, TimeUnit.MILLISECONDS, scheduler);

		left
		    .join(
		        right,
		        i -> Observable.timer(150, TimeUnit.MILLISECONDS, scheduler),
		        i -> Observable.timer(0, TimeUnit.MILLISECONDS, scheduler),
		        (l,r) -> Tuple.create(l, r)
		    )
		    .take(10)
		    .subscribe(tester);
		
		scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
		tester.assertReceivedOnNext(Arrays.asList(
			Tuple.create(0L, 0L),
			Tuple.create(0L, 1L),
			Tuple.create(1L, 1L),
			Tuple.create(1L, 2L),
			Tuple.create(2L, 2L),
			Tuple.create(2L, 3L),
			Tuple.create(3L, 3L),
			Tuple.create(3L, 4L),
			Tuple.create(4L, 4L),
			Tuple.create(4L, 5L)
		));
	}
}
