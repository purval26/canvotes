package com.canvotes

import com.canvotes.core.history.CanvasCommand
import com.canvotes.core.history.CommandHistory
import org.junit.Assert.*
import org.junit.Test

class CommandHistoryTest {

    @Test
    fun testUndoRedoExecution() {
        val history = CommandHistory()
        var counter = 0

        val command = object : CanvasCommand {
            override val description = "Increment"
            override fun execute() { counter += 10 }
            override fun undo() { counter -= 10 }
        }

        assertFalse(history.canUndo)
        assertFalse(history.canRedo)

        history.execute(command)
        assertEquals(10, counter)
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)

        val undid = history.undo()
        assertTrue(undid)
        assertEquals(0, counter)
        assertFalse(history.canUndo)
        assertTrue(history.canRedo)

        val redid = history.redo()
        assertTrue(redid)
        assertEquals(10, counter)
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)
    }

    @Test
    fun testNewExecutionClearsRedoStack() {
        val history = CommandHistory()
        var text = ""

        fun makeCmd(append: String) = object : CanvasCommand {
            override val description = "Append $append"
            override fun execute() { text += append }
            override fun undo() { text = text.removeSuffix(append) }
        }

        history.execute(makeCmd("A"))
        history.execute(makeCmd("B"))
        assertEquals("AB", text)

        history.undo()
        assertEquals("A", text)
        assertTrue(history.canRedo)

        // New command must invalidate redo stack
        history.execute(makeCmd("C"))
        assertEquals("AC", text)
        assertFalse(history.canRedo)
    }

    @Test
    fun testMaxCapacityEnforcement() {
        val capacity = 5
        val history = CommandHistory(maxCapacity = capacity)

        for (i in 1..10) {
            history.execute(object : CanvasCommand {
                override val description = "Cmd $i"
                override fun execute() {}
                override fun undo() {}
            })
        }

        assertEquals(capacity, history.undoCount)
    }
}
