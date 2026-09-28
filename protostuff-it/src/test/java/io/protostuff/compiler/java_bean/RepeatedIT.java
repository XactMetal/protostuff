package io.protostuff.compiler.java_bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import io.protostuff.compiler.it.java_bean.Int32List;
import io.protostuff.compiler.it.java_bean.UnmodifiableInt32List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import io.protostuff.Input;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;

/**
 * Integration tests for java_bean repeated fields
 *
 * @author Konstantin Shchepanovskyi
 */
public class RepeatedIT
{

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * Test that generated #mergeFrom method can be used multiple times
     *
     * @throws Exception
     */
    @Test
    public void testMergeTwice() throws Exception
    {
        Int32List list = Int32List.getSchema().newMessage();
        list.mergeFrom(createInput(42), list);
        list.mergeFrom(createInput(43), list);
        Assert.assertEquals(Arrays.asList(42, 43), list.getNumbersList());
    }

    @Test
    public void testEmptyRepeatedFieldIsEmptyList() throws Exception
    {
        Int32List list = new Int32List();
        Assert.assertNotNull(list.getNumbersList());
        Assert.assertTrue(list.getNumbersList().isEmpty());
        list.getNumbersList().add(7);
        Assert.assertEquals(Collections.singletonList(7), list.getNumbersList());

        Int32List cleared = new Int32List();
        cleared.setNumbersList(null);
        Assert.assertNotNull(cleared.getNumbersList());
        Assert.assertTrue(cleared.getNumbersList().isEmpty());

        Int32List fresh = new Int32List();
        Assert.assertEquals(fresh, cleared);
        Assert.assertEquals(fresh.hashCode(), cleared.hashCode());

        LinkedBuffer buffer = LinkedBuffer.allocate();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ProtostuffIOUtil.writeTo(outputStream, fresh, Int32List.getSchema(), buffer);
        byte[] bytes = outputStream.toByteArray();
        Assert.assertEquals(0, bytes.length);
        Int32List decoded = Int32List.getSchema().newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, decoded, Int32List.getSchema());
        Assert.assertNotNull(decoded.getNumbersList());
        Assert.assertTrue(decoded.getNumbersList().isEmpty());
        Assert.assertEquals(fresh, decoded);
    }

    @Test
    public void testUnmodifiableList() throws Exception
    {
        UnmodifiableInt32List list = UnmodifiableInt32List.getSchema().newMessage();
        list.mergeFrom(createInput(42), list);
        exception.expect(UnsupportedOperationException.class);
        list.mergeFrom(createInput(43), list);
    }

    private Input createInput(int result) throws IOException
    {
        Input input = Mockito.mock(Input.class);
        Mockito.when(input.readFieldNumber(Mockito.any(Schema.class)))
                .thenReturn(1)
                .thenReturn(0);
        Mockito.when(input.readInt32())
                .thenReturn(result);
        return input;
    }
}
