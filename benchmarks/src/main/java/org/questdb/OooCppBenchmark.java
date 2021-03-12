/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2020 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package org.questdb;

import io.questdb.griffin.SqlException;
import io.questdb.std.Os;
import io.questdb.std.Unsafe;
import io.questdb.std.Vect;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class OooCppBenchmark {
    private static final long BUFFER_MAX_SIZE = 256 * 1024 * 1024L;
    private static long buffer;
    private static final long MB = 1024 * 1024L;

    public static void main(String[] args) throws RunnerException, SqlException, IOException {

//        Options opt = new OptionsBuilder()
//                .include(OooCppBenchmark.class.getSimpleName())
//                .warmupIterations(0)
//                .measurementIterations(3)
//                .forks(1)
//                .build();
//
//        new Runner(opt).run();
        var r = new OooCppBenchmark();
        r.mallocBuffer();

        // warmup
        Vect.setMemoryDouble(
                buffer,
                -1L,
                1_000L * Double.BYTES
        );

        int iterations = 500;
        for(int i = 1; i < 50; i+=1) {
            var timeout1 = runDoubleKs(iterations, i);
            var timeout2 = runLongsKs(iterations, i, Long.MIN_VALUE);
            var timeout3 = runLongsKs(iterations, i, -1L);
            System.out.println("" + i + ", " + timeout1 + ", " + timeout2 + ", " + timeout3);
        }
        r.freeBuffer();
    }

    private static double runDoubleKs(int iterations, int i) {
        var nt = System.nanoTime();
        for (int j = 0; j < iterations; j++) {
            Vect.setMemoryDouble(
                    buffer,
                    -1.0,
                    i * MB / Double.BYTES
            );
        }
        var timeout = System.nanoTime() - nt;
        return Math.round(timeout * 1E-1 / iterations ) / 100.0;
    }

    private static double runLongsKs(int iterations, int i, long value) {
        var nt = System.nanoTime();
        for (int j = 0; j < iterations; j++) {
            Vect.setMemoryLong(
                    buffer,
                    value,
                    i * MB / Long.BYTES
            );
        }
        var timeout = System.nanoTime() - nt;
        return Math.round(timeout * 1E-1 / iterations ) / 100.0;
    }

    @Setup(Level.Trial)
    public void mallocBuffer() {
        Os.init();
        buffer = Unsafe.getUnsafe().allocateMemory(BUFFER_MAX_SIZE);
    }

    @TearDown(Level.Trial)
    public void freeBuffer() {
        Unsafe.free(buffer, BUFFER_MAX_SIZE);
    }

//    @Benchmark
//    public void testSetMemoryLong1MCheat() {
//        Vect.setMemoryLong(
//                buffer,
//                -1L,
//                1_000_000L * Long.BYTES
//        );
//    }
//
//    @Benchmark
//    public void testSetMemoryLong1kCheat() {
//        Vect.setMemoryLong(
//                buffer,
//                -1L,
//                1_000L * Long.BYTES
//        );
//    }
//
//    @Benchmark
//    public void testSetMemoryLong1M() {
//        Vect.setMemoryLong(
//                buffer,
//                Long.MIN_VALUE,
//                1_000_000L * Long.BYTES
//        );
//    }
//
//    @Benchmark
//    public void testSetMemoryLong1k() {
//        Vect.setMemoryLong(
//                buffer,
//                Long.MIN_VALUE,
//                1_000L * Long.BYTES
//        );
//    }
//
//    @Benchmark
//    public void testSetMemoryDouble1M() {
//        Vect.setMemoryDouble(
//                buffer,
//                Long.MIN_VALUE,
//                1_000_000L * Long.BYTES
//        );
//    }
//
//    @Benchmark
//    public void testSetMemoryDouble1k() {
//        Vect.setMemoryDouble(
//                buffer,
//                -1L,
//                1_000L * Double.BYTES
//        );
//    }


    @Benchmark
    public void testSetMemoryLong128k() {
        Vect.setMemoryLong(
                buffer,
                Long.MIN_VALUE,
                96_000L * Long.BYTES
        );
    }

    @Benchmark
    public void testSetMemoryDouble128k() {
        Vect.setMemoryDouble(
                buffer,
                -1L,
                96_000L * Double.BYTES
        );
    }
}
