/*
 * xTest
 * Copyright (C) 2015 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */
package ste.xtest.math;

import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 *
 */
public class ArrayRandomStub extends Random {
    private final int[] numbers;
    private int index;
    
    public ArrayRandomStub(final int[] numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("numbers can not be null");
        }
        if (numbers.length == 0) {
            throw new IllegalArgumentException("numbers can not be of length zero");
        }
        
        this.numbers = numbers;
        this.index = 0;
    }
    
    public int[] getNumbers() {
        return numbers;
    }
    
    public void reset() {
        this.index = 0;
    }
    
    // --------------------------------------------------------------- overrides
    
    @Override
    protected int next(int bits) {
        return numbers[(index++ % numbers.length)];
    }
    
    @Override
    public boolean nextBoolean() {
        return (nextInt() > 0);
    }
    
    @Override
    public void nextBytes(byte[] numbers) {
        for (int i=0; i<numbers.length; ++i) {
            numbers[i] = (byte)next(32);
        }
    }
    
    @Override
    public double nextDouble() {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public float nextFloat() {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public double nextGaussian() {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public int nextInt() {
        return next(32);
    }
    
    @Override
    public int nextInt(int bound) {
        return nextInt() % bound;
    }
    
    @Override
    public long nextLong() {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public DoubleStream doubles() {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public DoubleStream doubles(long streamSize) {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public IntStream ints() {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public IntStream ints(long streamSize) {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public LongStream longs() {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public LongStream longs(long streamSize) {
        throw new RuntimeException("not implemented");
    }
    
    @Override
    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        throw new RuntimeException("not implemented");
    }
    
}
