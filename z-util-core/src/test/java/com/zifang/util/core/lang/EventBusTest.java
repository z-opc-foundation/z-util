package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class EventBusTest {

    @Test
    public void testRegisterAndPost() {
        EventBus bus = new EventBus();
        MyListener l = new MyListener();
        bus.register(l);
        bus.post(new OrderEvent("O-1"));
        bus.post(new OrderEvent("O-2"));
        assertEquals(2, l.received.get());
        assertEquals("O-2", l.lastOrderId);
    }

    @Test
    public void testLambdaSubscribe() {
        EventBus bus = new EventBus();
        AtomicInteger n = new AtomicInteger();
        bus.subscribe(OrderEvent.class, e -> n.incrementAndGet());
        bus.post(new OrderEvent("X"));
        bus.post(new OrderEvent("Y"));
        assertEquals(2, n.get());
    }

    @Test
    public void testUnregister() {
        EventBus bus = new EventBus();
        MyListener l = new MyListener();
        bus.register(l);
        bus.post(new OrderEvent("1"));
        bus.unregister(l);
        bus.post(new OrderEvent("2"));
        assertEquals(1, l.received.get());
    }

    @Test
    public void testNullPostIgnored() {
        EventBus bus = new EventBus();
        // 不应抛异常
        bus.post(null);
    }

    static class OrderEvent {
        final String orderId;

        OrderEvent(String id) {
            this.orderId = id;
        }
    }

    static class MyListener {
        final AtomicInteger received = new AtomicInteger();
        String lastOrderId;

        @EventBus.Subscribe
        public void onOrder(OrderEvent e) {
            received.incrementAndGet();
            lastOrderId = e.orderId;
        }
    }
}