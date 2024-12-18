import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 *
 * Sample DICOM files to be used in the context of this project:
 * "ct-brain.dcm", and "hand.dcm".
 **/
public class App {
    /**
     * Given a 2D matrix containing floating-point values (i.e. a
     * graylevel image), create a new matrix of the same size where
     * range normalization has been applied. This means that the
     * minimum value in the source matrix must be mapped to 0.0, and
     * the maximum value must be mapped to 255.0.
     * @param matrix The matrix of interest.
     * @return The normalized matrix.
     */
    public static RealMatrix rangeNormalization(RealMatrix matrix) {
        RealMatrix NormalizedMatrix = matrix.copy();

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                double value = matrix.getEntry(i, j);
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
        }

        for (int i=0; i<matrix.getRowDimension(); i++) {
            for (int j=0; j< matrix.getColumnDimension(); j++){
                double NormalizedValue = (255 * (matrix.getEntry(i, j) - min)/(max - min));
                NormalizedMatrix.setEntry(i, j, NormalizedValue);
            }
        }
        return NormalizedMatrix;
    }

    /**
     * Compute the convolution of a 2D matrix (i.e. a graylevel image)
     * by a 2D convolution kernel. You must use the "valid"
     * convolution mode, which implies that no padding is required. If
     * the image is too small for the input kernel, an
     * "IllegalArgumentException()" must be thrown.
     * @param image The source 2D matrix.
     * @param kernel The convolution kernel.
     * @return The result of the convolution.
     */
    public static RealMatrix convolve(RealMatrix image,
                                      RealMatrix kernel) {
        int imageRows = image.getRowDimension();
        int imageCols = image.getColumnDimension();
        int kernelRows = kernel.getRowDimension();
        int kernelCols = kernel.getColumnDimension();


        if (imageRows < kernelRows || imageCols < kernelCols) {
            throw new IllegalArgumentException("Kernel is larger than the image");
        }

        int outputRows = imageRows - kernelRows + 1;
        int outputCols = imageCols - kernelCols + 1;

        RealMatrix output = new Array2DRowRealMatrix(outputRows, outputCols);

        for (int i = 0; i < outputRows; i++) {
            for (int j = 0; j < outputCols; j++) {
                double sum = 0;
                for (int m = 0; m < kernelRows; m++) {
                    for (int n = 0; n < kernelCols; n++) {
                        sum += image.getEntry(i + m, j + n) * kernel.getEntry(m, n);
                    }
                }
                output.setEntry(i, j, sum);
            }
        }

        return output;
    }

    /**
     * Compute the convolution of a 2D matrix (i.e. a graylevel image)
     * with the horizontal Sobel kernel (i.e. "dI/dx" in the
     * slides). Evidently, make sure to use the "convolve()" method
     * above.
     * @param image The source 2D matrix.
     * @return The result of the convolution.
     */
    public static RealMatrix sobelX(RealMatrix image) {
        double[][] sobelKernel = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };

        RealMatrix kernel = MatrixUtils.createRealMatrix(sobelKernel);

        return convolve(image, kernel);
    }

    /**
     * Compute the convolution of a 2D matrix (i.e. a graylevel image)
     * with the vertical Sobel kernel (i.e. "dI/dy" in the
     * slides). Evidently, make sure to use the "convolve()" method
     * defined above.
     * @param image The source 2D matrix.
     * @return The result of the convolution.
     */
    public static RealMatrix sobelY(RealMatrix image) {
        double[][] y = {
                {-1, -2, -1},
                {0, 0, 0},
                {1, 2, 1}
        };

        RealMatrix kernel = MatrixUtils.createRealMatrix(y);

        return convolve(image, kernel);
    }

    /**
     * Compute the approximate magnitude of the gradient of a 2D
     * matrix (i.e. a graylevel image), as obtained through the Sobel
     * kernels. By "approximate", we mean that you have to use the
     * formula using the absolute values from the slides (*not* the
     * formula with the square root). Evidently, make sure to use the
     * "sobelX()" and "sobelY()" methods defined above.
     * @param image The source 2D matrix.
     * @return 2D matrix containing the magnitude of the gradient.
     */
    public static RealMatrix sobelMagnitude(RealMatrix image) {
        RealMatrix sobelX = sobelX(image);
        RealMatrix sobelY = sobelY(image);
        RealMatrix sobelMagnitude = MatrixUtils.createRealMatrix(sobelX.getRowDimension(), sobelX.getColumnDimension());

        for (int i = 0; i < sobelX.getRowDimension(); i++) {
            for (int j = 0; j < sobelX.getColumnDimension(); j++) {
                double x = sobelX.getEntry(i, j);
                double y = sobelY.getEntry(i, j);
                double magnitude = Math.abs(x) + Math.abs(y);
                sobelMagnitude.setEntry(i, j, magnitude);
            }
        }

        return sobelMagnitude;
    }
}
