package com.zifang.util.core.pattern.memento;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * MementoTest类。
 */
public class MementoTest {

    // ==================== MementoContext Tests ====================

    @Test
    /**
     * testSaveAndRetrieve方法。
     */
    public void testSaveAndRetrieve() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1");
        context.save("state2");
        context.save("state3");

        assertEquals(3, context.size());
        assertEquals("state3", context.current());
        assertEquals(2, context.getPointer());
    }

    @Test
    /**
     * testNextAndPrevious方法。
     */
    public void testNextAndPrevious() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1");
        context.save("state2");
        context.save("state3");
        // pointer=2, at last element

        // At last state: hasNext=false, hasPrevious=true
        assertFalse(context.hasNext());
        assertTrue(context.hasPrevious());
        assertEquals("state3", context.current());

        // Go back one step
        assertEquals("state2", context.previous());
        // Now at position 1: hasNext=true, hasPrevious=true
        assertTrue(context.hasNext());
        assertTrue(context.hasPrevious());

        // Go forward one step
        assertEquals("state3", context.next());
        // Back at last: hasNext=false, hasPrevious=true
        assertFalse(context.hasNext());
        assertTrue(context.hasPrevious());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    /**
     * testPreviousAtFirstThrows方法。
     */
    public void testPreviousAtFirstThrows() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1");
        context.previous();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    /**
     * testNextAtLastThrows方法。
     */
    public void testNextAtLastThrows() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1");
        context.next();
    }

    @Test
    /**
     * testSaveAfterUndoClearsRedoHistory方法。
     */
    public void testSaveAfterUndoClearsRedoHistory() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1");
        context.save("state2");
        context.save("state3");

        context.previous(); // back to state2
        assertTrue(context.hasNext());

        context.save("state2_new"); // save after undo

        assertFalse(context.hasNext());
        assertEquals(3, context.size());
        assertEquals("state2_new", context.current());
    }

    @Test
    /**
     * testMoveTo方法。
     */
    public void testMoveTo() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1");
        context.save("state2");
        context.save("state3");

        assertEquals("state1", context.moveTo(0));
        assertEquals(0, context.getPointer());

        assertEquals("state2", context.moveTo(1));
        assertEquals(1, context.getPointer());

        assertEquals("state3", context.moveTo(2));
        assertEquals(2, context.getPointer());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    /**
     * testMoveToOutOfBoundsThrows方法。
     */
    public void testMoveToOutOfBoundsThrows() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1");
        context.moveTo(5);
    }

    @Test
    /**
     * testSaveWithLabel方法。
     */
    public void testSaveWithLabel() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1", "first");
        context.save("state2", "second");
        context.save("state3");

        Optional<String> result = context.moveToLabel("first");
        assertTrue(result.isPresent());
        assertEquals("state1", result.get());
    }

    @Test
    /**
     * testMoveToLabelNotFound方法。
     */
    public void testMoveToLabelNotFound() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1", "first");

        Optional<String> result = context.moveToLabel("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    /**
     * testStepForwardAndBackward方法。
     */
    public void testStepForwardAndBackward() {
        MementoContext<String> context = new MementoContext<>();
        for (int i = 0; i < 10; i++) {
            context.save("state" + i);
        }

        context.moveTo(0);
        assertEquals("state3", context.stepForward(3));
        assertEquals("state0", context.stepBackward(3));
    }

    @Test
    /**
     * testFirstAndLast方法。
     */
    public void testFirstAndLast() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1");
        context.save("state2");
        context.save("state3");

        assertEquals("state1", context.first());
        assertEquals("state3", context.last());
    }

    @Test
    /**
     * testClear方法。
     */
    public void testClear() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1");
        context.save("state2");
        context.clear();

        assertEquals(0, context.size());
        assertEquals(-1, context.getPointer());
        assertNull(context.current());
    }

    @Test
    /**
     * testCurrentSnapshot方法。
     */
    public void testCurrentSnapshot() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1", "label1", "desc1");

        MementoContext.Snapshot<String> snapshot = context.currentSnapshot();
        assertNotNull(snapshot);
        assertEquals("state1", snapshot.getState());
        assertEquals("label1", snapshot.getLabel());
        assertEquals("desc1", snapshot.getDescription());
    }

    @Test
    /**
     * testGetSnapshotMetas方法。
     */
    public void testGetSnapshotMetas() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1", "label1");
        context.save("state2", "label2");
        context.save("state3");

        List<MementoContext.SnapshotMeta> metas = context.getSnapshotMetas();
        assertEquals(3, metas.size());
        assertEquals("label1", metas.get(0).getLabel());
        assertEquals("label2", metas.get(1).getLabel());
        assertNull(metas.get(2).getLabel());
        assertFalse(metas.get(0).isCurrent());
        assertTrue(metas.get(2).isCurrent());
    }

    @Test
    /**
     * testMaxSize方法。
     */
    public void testMaxSize() {
        MementoContext<String> context = new MementoContext<>(3);
        context.save("state1");
        context.save("state2");
        context.save("state3");
        context.save("state4"); // should remove state1

        assertEquals(3, context.size());
        assertEquals("state2", context.first());
    }

    @Test
    /**
     * testCheckpoint方法。
     */
    public void testCheckpoint() {
        MementoContext<String> context = new MementoContext<>();
        context.save("state1");
        context.save("state2");
        context.save("state3");
        // pointer=2, list=[state1, state2, state3]

        String checkpointName = context.createCheckpoint("backup1");
        // creates another state3 snapshot, pointer=3, list=[state1, state2, state3, state3]

        context.previous();
        // pointer=2, current=state3

        context.previous();
        // pointer=1, current=state2

        assertEquals("state2", context.current());

        Optional<String> result = context.getCheckpoint("backup1");
        assertTrue(result.isPresent());
        assertEquals("state3", result.get());
    }

    @Test
    /**
     * testListener方法。
     */
    public void testListener() {
        MementoContext<String> context = new MementoContext<>();
        StringBuilder events = new StringBuilder();

        context.addListener((event, snapshot, pointer) -> {
            events.append(event).append(":").append(snapshot != null ? snapshot.getState() : null).append("|");
        });

        context.save("state1");
        context.save("state2");
        context.save("state3");
        // pointer=2 (last), need to go back before we can go forward
        context.previous();  // move back to state2
        context.next();  // move forward to state3
        context.clear();

        assertTrue(events.toString().contains("SAVE:state1"));
        assertTrue(events.toString().contains("SAVE:state2"));
        assertTrue(events.toString().contains("SAVE:state3"));
        assertTrue(events.toString().contains("UNDO:state2"));
        assertTrue(events.toString().contains("REDO:state3"));
        assertTrue(events.toString().contains("CLEAR:null"));
    }

    @Test
    /**
     * testEmptyContext方法。
     */
    public void testEmptyContext() {
        MementoContext<String> context = new MementoContext<>();

        assertEquals(0, context.size());
        assertEquals(-1, context.getPointer());
        assertNull(context.current());
        assertFalse(context.hasNext());
        assertFalse(context.hasPrevious());
        assertEquals(0, context.getUsedSize());
        assertEquals(100, context.getMaxSize());
    }

    // ==================== Originator Tests ====================

    @Test
    /**
     * testOriginatorBasic方法。
     */
    public void testOriginatorBasic() {
        Originator<String> originator = new Originator<>();
        originator.setState("state1");

        assertEquals("state1", originator.getState());

        Originator.Memento<String> memento = originator.save();
        assertEquals("state1", memento.getState());

        originator.setState("state2");
        assertEquals("state2", originator.getState());

        originator.restore(memento);
        assertEquals("state1", originator.getState());
    }

    @Test
    /**
     * testOriginatorWithValidator方法。
     */
    public void testOriginatorWithValidator() {
        Originator<String> originator = new Originator<>(
                state -> state != null && !state.isEmpty()
        );

        assertTrue(originator.setState("valid"));
        assertFalse(originator.setState(""));
        assertFalse(originator.setState(null));
        assertEquals("valid", originator.getState());
    }

    @Test
    /**
     * testOriginatorWithCopier方法。
     */
    public void testOriginatorWithCopier() {
        StringBuffer original = new StringBuffer("original");
        Originator<StringBuffer> originator = new Originator<>(
                null,
                sb -> new StringBuffer(sb.toString())
        );
        originator.setState(original);

        Optional<StringBuffer> copy = originator.getStateCopy();
        assertTrue(copy.isPresent());
        assertNotSame(original, copy.get());
        assertEquals("original", copy.get().toString());
    }

    @Test
    /**
     * testStateUpdater方法。
     */
    public void testStateUpdater() {
        Originator<String> originator = new Originator<>();
        originator.setState("initial");

        Originator.StateUpdater<String> updater = originator.updater();
        updater.backup();
        updater.update("newState");
        assertEquals("newState", originator.getState());

        updater.rollback();
        assertEquals("initial", originator.getState());
    }

    @Test
    /**
     * testOriginatorSaveWithLabel方法。
     */
    public void testOriginatorSaveWithLabel() {
        Originator<String> originator = new Originator<>();
        originator.setState("state1");

        Originator.Memento<String> memento = originator.save("label1");
        assertEquals("state1", memento.getState());
        assertEquals("label1", memento.getLabel());
    }

    @Test
    /**
     * testStateEquals方法。
     */
    public void testStateEquals() {
        Originator<String> originator = new Originator<>();
        originator.setState("state1");

        Originator.Memento<String> memento = originator.save();

        assertTrue(originator.stateEquals("state1"));
        assertFalse(originator.stateEquals("state2"));
        assertTrue(originator.stateEquals(memento));
    }

    @Test
    /**
     * testOriginatorContextIntegration方法。
     */
    public void testOriginatorContextIntegration() {
        Originator<String> originator = new Originator<>();
        MementoContext<String> context = new MementoContext<>();

        originator.setState("state1");
        originator.saveTo(context);

        originator.setState("state2");
        originator.saveTo(context, "label2");

        originator.restoreFrom(context);
        assertEquals("state2", originator.getState());

        context.previous();
        originator.restoreFrom(context);
        assertEquals("state1", originator.getState());
    }

    // ==================== MementoImpl Tests ====================

    @Test
    /**
     * testMementoImplBasic方法。
     */
    public void testMementoImplBasic() {
        MementoImpl<String> memento = MementoImpl.of("state1");
        assertEquals("state1", memento.getState());
        assertTrue(memento.getTimestamp() > 0);
    }

    @Test
    /**
     * testMementoImplWithLabel方法。
     */
    public void testMementoImplWithLabel() {
        MementoImpl<String> memento = MementoImpl.of("state1", "myLabel");
        assertEquals("state1", memento.getState());
        assertEquals("myLabel", memento.getLabel());
    }

    @Test
    /**
     * testMementoImplBuilder方法。
     */
    public void testMementoImplBuilder() {
        MementoImpl<String> memento = MementoImpl.<String>builder()
                .state("state1")
                .label("label1")
                .description("desc1")
                .build();

        assertEquals("state1", memento.getState());
        assertEquals("label1", memento.getLabel());
        assertEquals("desc1", memento.getDescription());
        assertTrue(memento.getTimestamp() > 0);
    }

    // ==================== MementoHistory Tests ====================

    @Test
    /**
     * testMementoHistoryBasic方法。
     */
    public void testMementoHistoryBasic() {
        Originator<String> originator = new Originator<>();
        MementoHistory<String> history = new MementoHistory<>(originator);

        originator.setState("state1");
        history.execute("state2");
        history.execute("state3");

        assertEquals("state3", originator.getState());

        assertTrue(history.undo());
        assertEquals("state2", originator.getState());

        assertTrue(history.redo());
        assertEquals("state3", originator.getState());
    }

    @Test
    /**
     * testMementoHistoryCannotUndoAtFirst方法。
     */
    public void testMementoHistoryCannotUndoAtFirst() {
        Originator<String> originator = new Originator<>();
        MementoHistory<String> history = new MementoHistory<>(originator);

        originator.setState("state1");
        history.execute("state2");

        assertFalse(history.undo());
    }

    @Test
    /**
     * testMementoHistoryCannotRedoAtLast方法。
     */
    public void testMementoHistoryCannotRedoAtLast() {
        Originator<String> originator = new Originator<>();
        MementoHistory<String> history = new MementoHistory<>(originator);

        originator.setState("state1");
        history.execute("state2");

        history.undo();
        assertFalse(history.redo());
    }

    @Test
    /**
     * testMementoHistoryClear方法。
     */
    public void testMementoHistoryClear() {
        Originator<String> originator = new Originator<>();
        MementoHistory<String> history = new MementoHistory<>(originator);

        originator.setState("state1");
        history.execute("state2");
        history.execute("state3");

        history.clearHistory();

        assertEquals(0, history.getUndoStackSize());
        assertEquals(0, history.getRedoStackSize());
    }

    @Test
    /**
     * testMementoHistoryGoTo方法。
     */
    public void testMementoHistoryGoTo() {
        Originator<String> originator = new Originator<>();
        MementoHistory<String> history = new MementoHistory<>(originator);

        originator.setState("state1");
        history.execute("state2");
        history.execute("state3");
        history.execute("state4");
        // context: [state2, state3, state4], pointer=2

        history.goTo(1);
        // context.moveTo(1) returns state3
        assertEquals("state3", originator.getState());
    }

    @Test
    /**
     * testMementoHistoryCheckpoint方法。
     */
    public void testMementoHistoryCheckpoint() {
        Originator<String> originator = new Originator<>();
        MementoHistory<String> history = new MementoHistory<>(originator);

        originator.setState("state1");
        history.execute("state2");
        history.execute("state3");
        // context: [state2, state3], pointer=1, originator=state3
        // undoStack: [null, state2] (first null from setState, then state2)

        String checkpointName = history.createCheckpoint("backup");
        // context saves state3 again: [state2, state3, state3], pointer=2

        history.undo();
        // originator=state3, undoStack=[state2], redoStack=[state3]
        // context pointer=1

        history.undo();
        // originator=state2, undoStack=[], redoStack=[state3, state2]
        // context pointer=0

        assertEquals("state2", originator.getState());

        Optional<String> result = history.goToCheckpoint("backup");
        assertTrue(result.isPresent());
        assertEquals("state3", originator.getState());
    }

    @Test
    /**
     * testMementoHistoryExecuteWithLabel方法。
     */
    public void testMementoHistoryExecuteWithLabel() {
        Originator<String> originator = new Originator<>();
        MementoHistory<String> history = new MementoHistory<>(originator);

        originator.setState("state1");
        history.execute("state2", "label2");
        history.execute("state3");

        List<MementoContext.SnapshotMeta> metas = history.getHistory();
        assertEquals(2, metas.size());
    }

    @Test
    /**
     * testMementoHistoryListener方法。
     */
    public void testMementoHistoryListener() {
        Originator<String> originator = new Originator<>();
        MementoHistory<String> history = new MementoHistory<>(originator);
        StringBuilder events = new StringBuilder();

        history.addListener((event, from, to) -> {
            events.append(event).append("|");
        });

        originator.setState("state1");
        history.execute("state2");
        history.execute("state3");
        // context: [state2, state3], pointer=1
        // undoStack: [null, state2]

        history.undo();
        // context pointer=0, originator=state2

        history.redo();
        // context pointer=1, originator=state3

        assertTrue(events.toString().contains("EXECUTE"));
        assertTrue(events.toString().contains("UNDO"));
        assertTrue(events.toString().contains("REDO"));
    }

    @Test
    /**
     * testMementoHistoryFilter方法。
     */
    public void testMementoHistoryFilter() {
        Originator<String> originator = new Originator<>();
        MementoHistory<String> history = new MementoHistory<>(originator);

        originator.setState("state1");
        history.execute("state2");
        history.execute("state3");
        history.execute("state4");

        List<MementoContext.SnapshotMeta> filtered = history.filterHistory(
                meta -> meta.getIndex() < 2
        );

        assertEquals(2, filtered.size());
    }

    @Test
    /**
     * testMementoHistoryMaxSize方法。
     */
    public void testMementoHistoryMaxSize() {
        Originator<String> originator = new Originator<>();
        MementoHistory<String> history = new MementoHistory<>(originator, 3);

        for (int i = 0; i < 10; i++) {
            originator.setState("state" + i);
            history.execute(originator.getState());
        }

        // Context should be limited to max size
        assertTrue(history.getContext().getUsedSize() <= 3);
    }

    @Test
    /**
     * testMementoHistoryGetContext方法。
     */
    public void testMementoHistoryGetContext() {
        Originator<String> originator = new Originator<>();
        MementoHistory<String> history = new MementoHistory<>(originator, 50);

        originator.setState("state1");
        history.execute("state2");
        // context only tracks execute calls, not setState
        // so context has 1 element: [state2]

        MementoContext<String> context = history.getContext();
        assertNotNull(context);
        assertEquals(1, context.size());
    }
}
