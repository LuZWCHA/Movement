/* ***** BEGIN LICENSE BLOCK *****
 * JTransforms
 * Copyright (c) 2007 onward, Piotr Wendykier
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ***** END LICENSE BLOCK ***** */
package org.jtransforms.dst;

import java.util.concurrent.Future;

import org.jtransforms.utils.CommonUtils;
import org.jtransforms.utils.ConcurrencyUtils;

//import pl.edu.icm.jlargearrays.ConcurrencyUtils;
//import pl.edu.icm.jlargearrays.DoubleLargeArray;
//import pl.edu.icm.jlargearrays.LargeArray;
import static com.nowandfuture.mod.utils.math.apache.FastMath.*;

/**
 * Computes 2D Discrete Sine Transform (DST) of double precision data. The sizes
 * of both dimensions can be arbitrary numbers. This is a parallel
 * implementation optimized for SMP systems.<br>
 * <br>
 * Part of code is derived from General Purpose FFT Package written by Takuya
 * Ooura (http://www.kurims.kyoto-u.ac.jp/~ooura/fft.html)
 *  
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 */
public class DoubleDST_2D
{

    private int rows;

    private int columns;

    private long rowsl;

    private long columnsl;

    private DoubleDST_1D dstColumns, dstRows;

    private boolean isPowerOfTwo = false;

    private boolean useThreads = false;

    /**
     * Creates new instance of DoubleDST_2D.
     *  
     * @param rows    number of rows
     * @param columns number of columns
     */
    public DoubleDST_2D(long rows, long columns)
    {
        if (rows <= 1 || columns <= 1) {
            throw new IllegalArgumentException("rows and columns must be greater than 1");
        }
        this.rows = (int) rows;
        this.columns = (int) columns;
        this.rowsl = rows;
        this.columnsl = columns;
        if (rows * columns >= CommonUtils.getThreadsBeginN_2D()) {
            useThreads = true;
        }
        if (CommonUtils.isPowerOf2(rows) && CommonUtils.isPowerOf2(columns)) {
            isPowerOfTwo = true;
        }
        CommonUtils.setUseLargeArrays(false/*rows * columns > LargeArray.getMaxSizeOf32bitArray()*/);
        dstRows = new DoubleDST_1D(rows);
        if (rows == columns) {
            dstColumns = dstRows;
        } else {
            dstColumns = new DoubleDST_1D(columns);
        }
    }

    /**
     * Computes 2D forward DST (DST-II) leaving the result in <code>a</code>.
     * The data is stored in 1D array in row-major order.
     *  
     * @param a     data to transform
     * @param scale if true then scaling is performed
     */
    public void forward(final double[] a, final boolean scale)
    {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if (isPowerOfTwo) {
            if ((nthreads > 1) && useThreads) {
                ddxt2d_subth(-1, a, scale);
                ddxt2d0_subth(-1, a, scale);
            } else {
                ddxt2d_sub(-1, a, scale);
                for (int i = 0; i < rows; i++) {
                    dstColumns.forward(a, i * columns, scale);
                }
            }
        } else if ((nthreads > 1) && useThreads && (rows >= nthreads) && (columns >= nthreads)) {
            Future<?>[] futures = new Future[nthreads];
            int p = rows / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final int firstRow = l * p;
                final int lastRow = (l == (nthreads - 1)) ? rows : firstRow + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        for (int i = firstRow; i < lastRow; i++) {
                            dstColumns.forward(a, i * columns, scale);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
            p = columns / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final int firstColumn = l * p;
                final int lastColumn = (l == (nthreads - 1)) ? columns : firstColumn + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        double[] temp = new double[rows];
                        for (int c = firstColumn; c < lastColumn; c++) {
                            for (int r = 0; r < rows; r++) {
                                temp[r] = a[r * columns + c];
                            }
                            dstRows.forward(temp, scale);
                            for (int r = 0; r < rows; r++) {
                                a[r * columns + c] = temp[r];
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < rows; i++) {
                dstColumns.forward(a, i * columns, scale);
            }
            double[] temp = new double[rows];
            for (int c = 0; c < columns; c++) {
                for (int r = 0; r < rows; r++) {
                    temp[r] = a[r * columns + c];
                }
                dstRows.forward(temp, scale);
                for (int r = 0; r < rows; r++) {
                    a[r * columns + c] = temp[r];
                }
            }
        }
    }

    /**
     * Computes 2D forward DST (DST-II) leaving the result in <code>a</code>.
     * The data is stored in 1D array in row-major order.
     *  
     * @param a     data to transform
     * @param scale if true then scaling is performed
     
    public void forward(final DoubleLargeArray a, final boolean scale)
    {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if (isPowerOfTwo) {
            if ((nthreads > 1) && useThreads) {
                ddxt2d_subth(-1, a, scale);
                ddxt2d0_subth(-1, a, scale);
            } else {
                ddxt2d_sub(-1, a, scale);
                for (long i = 0; i < rowsl; i++) {
                    dstColumns.forward(a, i * columnsl, scale);
                }
            }
        } else if ((nthreads > 1) && useThreads && (rowsl >= nthreads) && (columnsl >= nthreads)) {
            Future<?>[] futures = new Future[nthreads];
            long p = rowsl / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final long firstRow = l * p;
                final long lastRow = (l == (nthreads - 1)) ? rowsl : firstRow + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        for (long i = firstRow; i < lastRow; i++) {
                            dstColumns.forward(a, i * columnsl, scale);
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(futures);
            } catch (InterruptedException ex) {
                Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
            }
            p = columnsl / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final long firstColumn = l * p;
                final long lastColumn = (l == (nthreads - 1)) ? columnsl : firstColumn + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        DoubleLargeArray temp = new DoubleLargeArray(rowsl, false);
                        for (long c = firstColumn; c < lastColumn; c++) {
                            for (long r = 0; r < rowsl; r++) {
                                temp.setDouble(r, a.getDouble(r * columnsl + c));
                            }
                            dstRows.forward(temp, scale);
                            for (long r = 0; r < rowsl; r++) {
                                a.setDouble(r * columnsl + c, temp.getDouble(r));
                            }
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(futures);
            } catch (InterruptedException ex) {
                Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            for (long i = 0; i < rowsl; i++) {
                dstColumns.forward(a, i * columnsl, scale);
            }
            DoubleLargeArray temp = new DoubleLargeArray(rowsl, false);
            for (long c = 0; c < columnsl; c++) {
                for (long r = 0; r < rowsl; r++) {
                    temp.setDouble(r, a.getDouble(r * columnsl + c));
                }
                dstRows.forward(temp, scale);
                for (long r = 0; r < rowsl; r++) {
                    a.setDouble(r * columnsl + c, temp.getDouble(r));
                }
            }
        }
    }*/

    /**
     * Computes 2D forward DST (DST-II) leaving the result in <code>a</code>.
     * The data is stored in 2D array.
     *  
     * @param a     data to transform
     * @param scale if true then scaling is performed
     */
    public void forward(final double[][] a, final boolean scale)
    {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if (isPowerOfTwo) {
            if ((nthreads > 1) && useThreads) {
                ddxt2d_subth(-1, a, scale);
                ddxt2d0_subth(-1, a, scale);
            } else {
                ddxt2d_sub(-1, a, scale);
                for (int i = 0; i < rows; i++) {
                    dstColumns.forward(a[i], scale);
                }
            }
        } else if ((nthreads > 1) && useThreads && (rows >= nthreads) && (columns >= nthreads)) {
            Future<?>[] futures = new Future[nthreads];
            int p = rows / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final int firstRow = l * p;
                final int lastRow = (l == (nthreads - 1)) ? rows : firstRow + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        for (int i = firstRow; i < lastRow; i++) {
                            dstColumns.forward(a[i], scale);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
            p = columns / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final int firstColumn = l * p;
                final int lastColumn = (l == (nthreads - 1)) ? columns : firstColumn + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        double[] temp = new double[rows];
                        for (int c = firstColumn; c < lastColumn; c++) {
                            for (int r = 0; r < rows; r++) {
                                temp[r] = a[r][c];
                            }
                            dstRows.forward(temp, scale);
                            for (int r = 0; r < rows; r++) {
                                a[r][c] = temp[r];
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < rows; i++) {
                dstColumns.forward(a[i], scale);
            }
            double[] temp = new double[rows];
            for (int c = 0; c < columns; c++) {
                for (int r = 0; r < rows; r++) {
                    temp[r] = a[r][c];
                }
                dstRows.forward(temp, scale);
                for (int r = 0; r < rows; r++) {
                    a[r][c] = temp[r];
                }
            }
        }
    }

    /**
     * Computes 2D inverse DST (DST-III) leaving the result in <code>a</code>.
     * The data is stored in 1D array in row-major order.
     *  
     * @param a     data to transform
     * @param scale if true then scaling is performed
     */
    public void inverse(final double[] a, final boolean scale)
    {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if (isPowerOfTwo) {
            if ((nthreads > 1) && useThreads) {
                ddxt2d_subth(1, a, scale);
                ddxt2d0_subth(1, a, scale);
            } else {
                ddxt2d_sub(1, a, scale);
                for (int i = 0; i < rows; i++) {
                    dstColumns.inverse(a, i * columns, scale);
                }
            }
        } else if ((nthreads > 1) && useThreads && (rows >= nthreads) && (columns >= nthreads)) {
            Future<?>[] futures = new Future[nthreads];
            int p = rows / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final int firstRow = l * p;
                final int lastRow = (l == (nthreads - 1)) ? rows : firstRow + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        for (int i = firstRow; i < lastRow; i++) {
                            dstColumns.inverse(a, i * columns, scale);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
            p = columns / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final int firstColumn = l * p;
                final int lastColumn = (l == (nthreads - 1)) ? columns : firstColumn + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        double[] temp = new double[rows];
                        for (int c = firstColumn; c < lastColumn; c++) {
                            for (int r = 0; r < rows; r++) {
                                temp[r] = a[r * columns + c];
                            }
                            dstRows.inverse(temp, scale);
                            for (int r = 0; r < rows; r++) {
                                a[r * columns + c] = temp[r];
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < rows; i++) {
                dstColumns.inverse(a, i * columns, scale);
            }
            double[] temp = new double[rows];
            for (int c = 0; c < columns; c++) {
                for (int r = 0; r < rows; r++) {
                    temp[r] = a[r * columns + c];
                }
                dstRows.inverse(temp, scale);
                for (int r = 0; r < rows; r++) {
                    a[r * columns + c] = temp[r];
                }
            }
        }
    }

    /**
     * Computes 2D inverse DST (DST-III) leaving the result in <code>a</code>.
     * The data is stored in 1D array in row-major order.
     *  
     * @param a     data to transform
     * @param scale if true then scaling is performed
     
    public void inverse(final DoubleLargeArray a, final boolean scale)
    {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if (isPowerOfTwo) {
            if ((nthreads > 1) && useThreads) {
                ddxt2d_subth(1, a, scale);
                ddxt2d0_subth(1, a, scale);
            } else {
                ddxt2d_sub(1, a, scale);
                for (long i = 0; i < rowsl; i++) {
                    dstColumns.inverse(a, i * columnsl, scale);
                }
            }
        } else if ((nthreads > 1) && useThreads && (rowsl >= nthreads) && (columnsl >= nthreads)) {
            Future<?>[] futures = new Future[nthreads];
            long p = rowsl / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final long firstRow = l * p;
                final long lastRow = (l == (nthreads - 1)) ? rowsl : firstRow + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        for (long i = firstRow; i < lastRow; i++) {
                            dstColumns.inverse(a, i * columnsl, scale);
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(futures);
            } catch (InterruptedException ex) {
                Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
            }
            p = columnsl / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final long firstColumn = l * p;
                final long lastColumn = (l == (nthreads - 1)) ? columnsl : firstColumn + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        DoubleLargeArray temp = new DoubleLargeArray(rowsl, false);
                        for (long c = firstColumn; c < lastColumn; c++) {
                            for (long r = 0; r < rowsl; r++) {
                                temp.setDouble(r, a.getDouble(r * columnsl + c));
                            }
                            dstRows.inverse(temp, scale);
                            for (long r = 0; r < rowsl; r++) {
                                a.setDouble(r * columnsl + c, temp.getDouble(r));
                            }
                        }
                    }
                });
            }
            try {
                ConcurrencyUtils.waitForCompletion(futures);
            } catch (InterruptedException ex) {
                Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            for (long i = 0; i < rowsl; i++) {
                dstColumns.inverse(a, i * columnsl, scale);
            }
            DoubleLargeArray temp = new DoubleLargeArray(rowsl, false);
            for (long c = 0; c < columnsl; c++) {
                for (long r = 0; r < rowsl; r++) {
                    temp.setDouble(r, a.getDouble(r * columnsl + c));
                }
                dstRows.inverse(temp, scale);
                for (long r = 0; r < rowsl; r++) {
                    a.setDouble(r * columnsl + c, temp.getDouble(r));
                }
            }
        }
    }*/

    /**
     * Computes 2D inverse DST (DST-III) leaving the result in <code>a</code>.
     * The data is stored in 2D array.
     *  
     * @param a     data to transform
     * @param scale if true then scaling is performed
     */
    public void inverse(final double[][] a, final boolean scale)
    {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if (isPowerOfTwo) {
            if ((nthreads > 1) && useThreads) {
                ddxt2d_subth(1, a, scale);
                ddxt2d0_subth(1, a, scale);
            } else {
                ddxt2d_sub(1, a, scale);
                for (int i = 0; i < rows; i++) {
                    dstColumns.inverse(a[i], scale);
                }
            }
        } else if ((nthreads > 1) && useThreads && (rows >= nthreads) && (columns >= nthreads)) {
            Future<?>[] futures = new Future[nthreads];
            int p = rows / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final int firstRow = l * p;
                final int lastRow = (l == (nthreads - 1)) ? rows : firstRow + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        for (int i = firstRow; i < lastRow; i++) {
                            dstColumns.inverse(a[i], scale);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
            p = columns / nthreads;
            for (int l = 0; l < nthreads; l++) {
                final int firstColumn = l * p;
                final int lastColumn = (l == (nthreads - 1)) ? columns : firstColumn + p;
                futures[l] = ConcurrencyUtils.submit(new Runnable()
                {
                    public void run()
                    {
                        double[] temp = new double[rows];
                        for (int c = firstColumn; c < lastColumn; c++) {
                            for (int r = 0; r < rows; r++) {
                                temp[r] = a[r][c];
                            }
                            dstRows.inverse(temp, scale);
                            for (int r = 0; r < rows; r++) {
                                a[r][c] = temp[r];
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int i = 0; i < rows; i++) {
                dstColumns.inverse(a[i], scale);
            }
            double[] temp = new double[rows];
            for (int c = 0; c < columns; c++) {
                for (int r = 0; r < rows; r++) {
                    temp[r] = a[r][c];
                }
                dstRows.inverse(temp, scale);
                for (int r = 0; r < rows; r++) {
                    a[r][c] = temp[r];
                }
            }
        }
    }

    private void ddxt2d_subth(final int isgn, final double[] a, final boolean scale)
    {
        int nthread = min(columns, ConcurrencyUtils.getNumberOfThreads());
        int nt = 4 * rows;
        if (columns == 2) {
            nt >>= 1;
        } else if (columns < 2) {
            nt >>= 2;
        }
        final int ntf = nt;
        final int nthreads = nthread;
        Future<?>[] futures = new Future[nthreads];

        for (int i = 0; i < nthreads; i++) {
            final int n0 = i;
            futures[i] = ConcurrencyUtils.submit(new Runnable()
            {
                public void run()
                {
                    int idx1, idx2;
                    double[] t = new double[ntf];
                    if (columns > 2) {
                        if (isgn == -1) {
                            for (int c = 4 * n0; c < columns; c += 4 * nthreads) {
                                for (int r = 0; r < rows; r++) {
                                    idx1 = r * columns + c;
                                    idx2 = rows + r;
                                    t[r] = a[idx1];
                                    t[idx2] = a[idx1 + 1];
                                    t[idx2 + rows] = a[idx1 + 2];
                                    t[idx2 + 2 * rows] = a[idx1 + 3];
                                }
                                dstRows.forward(t, 0, scale);
                                dstRows.forward(t, rows, scale);
                                dstRows.forward(t, 2 * rows, scale);
                                dstRows.forward(t, 3 * rows, scale);
                                for (int r = 0; r < rows; r++) {
                                    idx1 = r * columns + c;
                                    idx2 = rows + r;
                                    a[idx1] = t[r];
                                    a[idx1 + 1] = t[idx2];
                                    a[idx1 + 2] = t[idx2 + rows];
                                    a[idx1 + 3] = t[idx2 + 2 * rows];
                                }
                            }
                        } else {
                            for (int c = 4 * n0; c < columns; c += 4 * nthreads) {
                                for (int r = 0; r < rows; r++) {
                                    idx1 = r * columns + c;
                                    idx2 = rows + r;
                                    t[r] = a[idx1];
                                    t[idx2] = a[idx1 + 1];
                                    t[idx2 + rows] = a[idx1 + 2];
                                    t[idx2 + 2 * rows] = a[idx1 + 3];
                                }
                                dstRows.inverse(t, 0, scale);
                                dstRows.inverse(t, rows, scale);
                                dstRows.inverse(t, 2 * rows, scale);
                                dstRows.inverse(t, 3 * rows, scale);
                                for (int r = 0; r < rows; r++) {
                                    idx1 = r * columns + c;
                                    idx2 = rows + r;
                                    a[idx1] = t[r];
                                    a[idx1 + 1] = t[idx2];
                                    a[idx1 + 2] = t[idx2 + rows];
                                    a[idx1 + 3] = t[idx2 + 2 * rows];
                                }
                            }
                        }
                    } else if (columns == 2) {
                        for (int r = 0; r < rows; r++) {
                            idx1 = r * columns + 2 * n0;
                            idx2 = r;
                            t[idx2] = a[idx1];
                            t[idx2 + rows] = a[idx1 + 1];
                        }
                        if (isgn == -1) {
                            dstRows.forward(t, 0, scale);
                            dstRows.forward(t, rows, scale);
                        } else {
                            dstRows.inverse(t, 0, scale);
                            dstRows.inverse(t, rows, scale);
                        }
                        for (int r = 0; r < rows; r++) {
                            idx1 = r * columns + 2 * n0;
                            idx2 = r;
                            a[idx1] = t[idx2];
                            a[idx1 + 1] = t[idx2 + rows];
                        }
                    }
                }
            });
        }
        ConcurrencyUtils.waitForCompletion(futures);
    }

    /*private void ddxt2d_subth(final int isgn, final DoubleLargeArray a, final boolean scale)
    {
        int nthread = (int) min(columnsl, ConcurrencyUtils.getNumberOfThreads());
        long nt = 4 * rowsl;
        if (columnsl == 2) {
            nt >>= 1;
        } else if (columnsl < 2) {
            nt >>= 2;
        }
        final long ntf = nt;
        final int nthreads = nthread;
        Future<?>[] futures = new Future[nthreads];

        for (int i = 0; i < nthreads; i++) {
            final long n0 = i;
            futures[i] = ConcurrencyUtils.submit(new Runnable()
            {
                public void run()
                {
                    long idx1, idx2;
                    DoubleLargeArray t = new DoubleLargeArray(ntf);
                    if (columnsl > 2) {
                        if (isgn == -1) {
                            for (long c = 4 * n0; c < columnsl; c += 4 * nthreads) {
                                for (long r = 0; r < rowsl; r++) {
                                    idx1 = r * columnsl + c;
                                    idx2 = rowsl + r;
                                    t.setDouble(r, a.getDouble(idx1));
                                    t.setDouble(idx2, a.getDouble(idx1 + 1));
                                    t.setDouble(idx2 + rowsl, a.getDouble(idx1 + 2));
                                    t.setDouble(idx2 + 2 * rowsl, a.getDouble(idx1 + 3));
                                }
                                dstRows.forward(t, 0, scale);
                                dstRows.forward(t, rowsl, scale);
                                dstRows.forward(t, 2 * rowsl, scale);
                                dstRows.forward(t, 3 * rowsl, scale);
                                for (long r = 0; r < rowsl; r++) {
                                    idx1 = r * columnsl + c;
                                    idx2 = rowsl + r;
                                    a.setDouble(idx1, t.getDouble(r));
                                    a.setDouble(idx1 + 1, t.getDouble(idx2));
                                    a.setDouble(idx1 + 2, t.getDouble(idx2 + rowsl));
                                    a.setDouble(idx1 + 3, t.getDouble(idx2 + 2 * rowsl));
                                }
                            }
                        } else {
                            for (long c = 4 * n0; c < columnsl; c += 4 * nthreads) {
                                for (long r = 0; r < rowsl; r++) {
                                    idx1 = r * columnsl + c;
                                    idx2 = rowsl + r;
                                    t.setDouble(r, a.getDouble(idx1));
                                    t.setDouble(idx2, a.getDouble(idx1 + 1));
                                    t.setDouble(idx2 + rowsl, a.getDouble(idx1 + 2));
                                    t.setDouble(idx2 + 2 * rowsl, a.getDouble(idx1 + 3));
                                }
                                dstRows.inverse(t, 0, scale);
                                dstRows.inverse(t, rowsl, scale);
                                dstRows.inverse(t, 2 * rowsl, scale);
                                dstRows.inverse(t, 3 * rowsl, scale);
                                for (long r = 0; r < rowsl; r++) {
                                    idx1 = r * columnsl + c;
                                    idx2 = rowsl + r;
                                    a.setDouble(idx1, t.getDouble(r));
                                    a.setDouble(idx1 + 1, t.getDouble(idx2));
                                    a.setDouble(idx1 + 2, t.getDouble(idx2 + rowsl));
                                    a.setDouble(idx1 + 3, t.getDouble(idx2 + 2 * rowsl));
                                }
                            }
                        }
                    } else if (columnsl == 2) {
                        for (long r = 0; r < rowsl; r++) {
                            idx1 = r * columnsl + 2 * n0;
                            idx2 = r;
                            t.setDouble(idx2, a.getDouble(idx1));
                            t.setDouble(idx2 + rowsl, a.getDouble(idx1 + 1));
                        }
                        if (isgn == -1) {
                            dstRows.forward(t, 0, scale);
                            dstRows.forward(t, rowsl, scale);
                        } else {
                            dstRows.inverse(t, 0, scale);
                            dstRows.inverse(t, rowsl, scale);
                        }
                        for (long r = 0; r < rowsl; r++) {
                            idx1 = r * columnsl + 2 * n0;
                            idx2 = r;
                            a.setDouble(idx1, t.getDouble(idx2));
                            a.setDouble(idx1 + 1, t.getDouble(idx2 + rowsl));
                        }
                    }
                }
            });
        }
        try {
            ConcurrencyUtils.waitForCompletion(futures);
        } catch (InterruptedException ex) {
            Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/

    private void ddxt2d_subth(final int isgn, final double[][] a, final boolean scale)
    {
        int nthread = min(columns, ConcurrencyUtils.getNumberOfThreads());
        int nt = 4 * rows;
        if (columns == 2) {
            nt >>= 1;
        } else if (columns < 2) {
            nt >>= 2;
        }
        final int ntf = nt;
        final int nthreads = nthread;
        Future<?>[] futures = new Future[nthreads];

        for (int i = 0; i < nthreads; i++) {
            final int n0 = i;
            futures[i] = ConcurrencyUtils.submit(new Runnable()
            {
                public void run()
                {
                    int idx2;
                    double[] t = new double[ntf];
                    if (columns > 2) {
                        if (isgn == -1) {
                            for (int c = 4 * n0; c < columns; c += 4 * nthreads) {
                                for (int r = 0; r < rows; r++) {
                                    idx2 = rows + r;
                                    t[r] = a[r][c];
                                    t[idx2] = a[r][c + 1];
                                    t[idx2 + rows] = a[r][c + 2];
                                    t[idx2 + 2 * rows] = a[r][c + 3];
                                }
                                dstRows.forward(t, 0, scale);
                                dstRows.forward(t, rows, scale);
                                dstRows.forward(t, 2 * rows, scale);
                                dstRows.forward(t, 3 * rows, scale);
                                for (int r = 0; r < rows; r++) {
                                    idx2 = rows + r;
                                    a[r][c] = t[r];
                                    a[r][c + 1] = t[idx2];
                                    a[r][c + 2] = t[idx2 + rows];
                                    a[r][c + 3] = t[idx2 + 2 * rows];
                                }
                            }
                        } else {
                            for (int c = 4 * n0; c < columns; c += 4 * nthreads) {
                                for (int r = 0; r < rows; r++) {
                                    idx2 = rows + r;
                                    t[r] = a[r][c];
                                    t[idx2] = a[r][c + 1];
                                    t[idx2 + rows] = a[r][c + 2];
                                    t[idx2 + 2 * rows] = a[r][c + 3];
                                }
                                dstRows.inverse(t, 0, scale);
                                dstRows.inverse(t, rows, scale);
                                dstRows.inverse(t, 2 * rows, scale);
                                dstRows.inverse(t, 3 * rows, scale);
                                for (int r = 0; r < rows; r++) {
                                    idx2 = rows + r;
                                    a[r][c] = t[r];
                                    a[r][c + 1] = t[idx2];
                                    a[r][c + 2] = t[idx2 + rows];
                                    a[r][c + 3] = t[idx2 + 2 * rows];
                                }
                            }
                        }
                    } else if (columns == 2) {
                        for (int r = 0; r < rows; r++) {
                            idx2 = r;
                            t[idx2] = a[r][2 * n0];
                            t[idx2 + rows] = a[r][2 * n0 + 1];
                        }
                        if (isgn == -1) {
                            dstRows.forward(t, 0, scale);
                            dstRows.forward(t, rows, scale);
                        } else {
                            dstRows.inverse(t, 0, scale);
                            dstRows.inverse(t, rows, scale);
                        }
                        for (int r = 0; r < rows; r++) {
                            idx2 = r;
                            a[r][2 * n0] = t[idx2];
                            a[r][2 * n0 + 1] = t[idx2 + rows];
                        }
                    }
                }
            });
        }
        ConcurrencyUtils.waitForCompletion(futures);
    }

    private void ddxt2d0_subth(final int isgn, final double[] a, final boolean scale)
    {
        final int nthreads = ConcurrencyUtils.getNumberOfThreads() > rows ? rows : ConcurrencyUtils.getNumberOfThreads();

        Future<?>[] futures = new Future[nthreads];

        for (int i = 0; i < nthreads; i++) {
            final int n0 = i;
            futures[i] = ConcurrencyUtils.submit(new Runnable()
            {

                public void run()
                {
                    if (isgn == -1) {
                        for (int r = n0; r < rows; r += nthreads) {
                            dstColumns.forward(a, r * columns, scale);
                        }
                    } else {
                        for (int r = n0; r < rows; r += nthreads) {
                            dstColumns.inverse(a, r * columns, scale);
                        }
                    }
                }
            });
        }
        ConcurrencyUtils.waitForCompletion(futures);
    }

    /*private void ddxt2d0_subth(final int isgn, final DoubleLargeArray a, final boolean scale)
    {
        final int nthreads = (int) (ConcurrencyUtils.getNumberOfThreads() > rowsl ? rowsl : ConcurrencyUtils.getNumberOfThreads());

        Future<?>[] futures = new Future[nthreads];

        for (int i = 0; i < nthreads; i++) {
            final long n0 = i;
            futures[i] = ConcurrencyUtils.submit(new Runnable()
            {

                public void run()
                {
                    if (isgn == -1) {
                        for (long r = n0; r < rowsl; r += nthreads) {
                            dstColumns.forward(a, r * columnsl, scale);
                        }
                    } else {
                        for (long r = n0; r < rows; r += nthreads) {
                            dstColumns.inverse(a, r * columnsl, scale);
                        }
                    }
                }
            });
        }
        try {
            ConcurrencyUtils.waitForCompletion(futures);
        } catch (InterruptedException ex) {
            Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(DoubleDST_2D.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/

    private void ddxt2d0_subth(final int isgn, final double[][] a, final boolean scale)
    {
        final int nthreads = ConcurrencyUtils.getNumberOfThreads() > rows ? rows : ConcurrencyUtils.getNumberOfThreads();

        Future<?>[] futures = new Future[nthreads];

        for (int i = 0; i < nthreads; i++) {
            final int n0 = i;
            futures[i] = ConcurrencyUtils.submit(new Runnable()
            {

                public void run()
                {
                    if (isgn == -1) {
                        for (int r = n0; r < rows; r += nthreads) {
                            dstColumns.forward(a[r], scale);
                        }
                    } else {
                        for (int r = n0; r < rows; r += nthreads) {
                            dstColumns.inverse(a[r], scale);
                        }
                    }
                }
            });
        }
        ConcurrencyUtils.waitForCompletion(futures);
    }

    private void ddxt2d_sub(int isgn, double[] a, boolean scale)
    {
        int idx1, idx2;
        int nt = 4 * rows;
        if (columns == 2) {
            nt >>= 1;
        } else if (columns < 2) {
            nt >>= 2;
        }
        double[] t = new double[nt];
        if (columns > 2) {
            if (isgn == -1) {
                for (int c = 0; c < columns; c += 4) {
                    for (int r = 0; r < rows; r++) {
                        idx1 = r * columns + c;
                        idx2 = rows + r;
                        t[r] = a[idx1];
                        t[idx2] = a[idx1 + 1];
                        t[idx2 + rows] = a[idx1 + 2];
                        t[idx2 + 2 * rows] = a[idx1 + 3];
                    }
                    dstRows.forward(t, 0, scale);
                    dstRows.forward(t, rows, scale);
                    dstRows.forward(t, 2 * rows, scale);
                    dstRows.forward(t, 3 * rows, scale);
                    for (int r = 0; r < rows; r++) {
                        idx1 = r * columns + c;
                        idx2 = rows + r;
                        a[idx1] = t[r];
                        a[idx1 + 1] = t[idx2];
                        a[idx1 + 2] = t[idx2 + rows];
                        a[idx1 + 3] = t[idx2 + 2 * rows];
                    }
                }
            } else {
                for (int c = 0; c < columns; c += 4) {
                    for (int r = 0; r < rows; r++) {
                        idx1 = r * columns + c;
                        idx2 = rows + r;
                        t[r] = a[idx1];
                        t[idx2] = a[idx1 + 1];
                        t[idx2 + rows] = a[idx1 + 2];
                        t[idx2 + 2 * rows] = a[idx1 + 3];
                    }
                    dstRows.inverse(t, 0, scale);
                    dstRows.inverse(t, rows, scale);
                    dstRows.inverse(t, 2 * rows, scale);
                    dstRows.inverse(t, 3 * rows, scale);
                    for (int r = 0; r < rows; r++) {
                        idx1 = r * columns + c;
                        idx2 = rows + r;
                        a[idx1] = t[r];
                        a[idx1 + 1] = t[idx2];
                        a[idx1 + 2] = t[idx2 + rows];
                        a[idx1 + 3] = t[idx2 + 2 * rows];
                    }
                }
            }
        } else if (columns == 2) {
            for (int r = 0; r < rows; r++) {
                idx1 = r * columns;
                t[r] = a[idx1];
                t[rows + r] = a[idx1 + 1];
            }
            if (isgn == -1) {
                dstRows.forward(t, 0, scale);
                dstRows.forward(t, rows, scale);
            } else {
                dstRows.inverse(t, 0, scale);
                dstRows.inverse(t, rows, scale);
            }
            for (int r = 0; r < rows; r++) {
                idx1 = r * columns;
                a[idx1] = t[r];
                a[idx1 + 1] = t[rows + r];
            }
        }
    }

    /*private void ddxt2d_sub(int isgn, DoubleLargeArray a, boolean scale)
    {
        long idx1, idx2;
        long nt = 4 * rowsl;
        if (columnsl == 2) {
            nt >>= 1;
        } else if (columnsl < 2) {
            nt >>= 2;
        }
        DoubleLargeArray t = new DoubleLargeArray(nt);
        if (columnsl > 2) {
            if (isgn == -1) {
                for (long c = 0; c < columnsl; c += 4) {
                    for (long r = 0; r < rowsl; r++) {
                        idx1 = r * columnsl + c;
                        idx2 = rowsl + r;
                        t.setDouble(r, a.getDouble(idx1));
                        t.setDouble(idx2, a.getDouble(idx1 + 1));
                        t.setDouble(idx2 + rowsl, a.getDouble(idx1 + 2));
                        t.setDouble(idx2 + 2 * rowsl, a.getDouble(idx1 + 3));
                    }
                    dstRows.forward(t, 0, scale);
                    dstRows.forward(t, rowsl, scale);
                    dstRows.forward(t, 2 * rowsl, scale);
                    dstRows.forward(t, 3 * rowsl, scale);
                    for (long r = 0; r < rowsl; r++) {
                        idx1 = r * columnsl + c;
                        idx2 = rowsl + r;
                        a.setDouble(idx1, t.getDouble(r));
                        a.setDouble(idx1 + 1, t.getDouble(idx2));
                        a.setDouble(idx1 + 2, t.getDouble(idx2 + rowsl));
                        a.setDouble(idx1 + 3, t.getDouble(idx2 + 2 * rowsl));
                    }
                }
            } else {
                for (long c = 0; c < columnsl; c += 4) {
                    for (long r = 0; r < rowsl; r++) {
                        idx1 = r * columnsl + c;
                        idx2 = rowsl + r;
                        t.setDouble(r, a.getDouble(idx1));
                        t.setDouble(idx2, a.getDouble(idx1 + 1));
                        t.setDouble(idx2 + rowsl, a.getDouble(idx1 + 2));
                        t.setDouble(idx2 + 2 * rowsl, a.getDouble(idx1 + 3));
                    }
                    dstRows.inverse(t, 0, scale);
                    dstRows.inverse(t, rowsl, scale);
                    dstRows.inverse(t, 2 * rowsl, scale);
                    dstRows.inverse(t, 3 * rowsl, scale);
                    for (long r = 0; r < rowsl; r++) {
                        idx1 = r * columnsl + c;
                        idx2 = rowsl + r;
                        a.setDouble(idx1, t.getDouble(r));
                        a.setDouble(idx1 + 1, t.getDouble(idx2));
                        a.setDouble(idx1 + 2, t.getDouble(idx2 + rowsl));
                        a.setDouble(idx1 + 3, t.getDouble(idx2 + 2 * rowsl));
                    }
                }
            }
        } else if (columnsl == 2) {
            for (long r = 0; r < rowsl; r++) {
                idx1 = r * columnsl;
                t.setDouble(r, a.getDouble(idx1));
                t.setDouble(rowsl + r, a.getDouble(idx1 + 1));
            }
            if (isgn == -1) {
                dstRows.forward(t, 0, scale);
                dstRows.forward(t, rowsl, scale);
            } else {
                dstRows.inverse(t, 0, scale);
                dstRows.inverse(t, rowsl, scale);
            }
            for (long r = 0; r < rowsl; r++) {
                idx1 = r * columnsl;
                a.setDouble(idx1, t.getDouble(r));
                a.setDouble(idx1 + 1, t.getDouble(rowsl + r));
            }
        }
    }*/

    private void ddxt2d_sub(int isgn, double[][] a, boolean scale)
    {
        int idx2;
        int nt = 4 * rows;
        if (columns == 2) {
            nt >>= 1;
        } else if (columns < 2) {
            nt >>= 2;
        }
        double[] t = new double[nt];
        if (columns > 2) {
            if (isgn == -1) {
                for (int c = 0; c < columns; c += 4) {
                    for (int r = 0; r < rows; r++) {
                        idx2 = rows + r;
                        t[r] = a[r][c];
                        t[idx2] = a[r][c + 1];
                        t[idx2 + rows] = a[r][c + 2];
                        t[idx2 + 2 * rows] = a[r][c + 3];
                    }
                    dstRows.forward(t, 0, scale);
                    dstRows.forward(t, rows, scale);
                    dstRows.forward(t, 2 * rows, scale);
                    dstRows.forward(t, 3 * rows, scale);
                    for (int r = 0; r < rows; r++) {
                        idx2 = rows + r;
                        a[r][c] = t[r];
                        a[r][c + 1] = t[idx2];
                        a[r][c + 2] = t[idx2 + rows];
                        a[r][c + 3] = t[idx2 + 2 * rows];
                    }
                }
            } else {
                for (int c = 0; c < columns; c += 4) {
                    for (int r = 0; r < rows; r++) {
                        idx2 = rows + r;
                        t[r] = a[r][c];
                        t[idx2] = a[r][c + 1];
                        t[idx2 + rows] = a[r][c + 2];
                        t[idx2 + 2 * rows] = a[r][c + 3];
                    }
                    dstRows.inverse(t, 0, scale);
                    dstRows.inverse(t, rows, scale);
                    dstRows.inverse(t, 2 * rows, scale);
                    dstRows.inverse(t, 3 * rows, scale);
                    for (int r = 0; r < rows; r++) {
                        idx2 = rows + r;
                        a[r][c] = t[r];
                        a[r][c + 1] = t[idx2];
                        a[r][c + 2] = t[idx2 + rows];
                        a[r][c + 3] = t[idx2 + 2 * rows];
                    }
                }
            }
        } else if (columns == 2) {
            for (int r = 0; r < rows; r++) {
                t[r] = a[r][0];
                t[rows + r] = a[r][1];
            }
            if (isgn == -1) {
                dstRows.forward(t, 0, scale);
                dstRows.forward(t, rows, scale);
            } else {
                dstRows.inverse(t, 0, scale);
                dstRows.inverse(t, rows, scale);
            }
            for (int r = 0; r < rows; r++) {
                a[r][0] = t[r];
                a[r][1] = t[rows + r];
            }
        }
    }
}
