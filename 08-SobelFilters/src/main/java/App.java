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
        // TODO
        return null;
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
        // TODO
        return null;
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
        // TODO
        return null;
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
        // TODO
        return null;
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
        // TODO
        return null;
    }
}
