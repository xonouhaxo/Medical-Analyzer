import be.uclouvain.ComplexNumber;
import be.uclouvain.EDFTimeSeries;
import be.uclouvain.Signal;

import java.io.IOException;
/**
 * Your task is to implement this class, by developing methods that
 * will be deployed as routes in the REST API of the Web application.
 *
 * Sample EDF files to be used in the context of this project:
 * "test_generator_2.edf" and "slow_drifts.edf".
 **/
public class App {


    /**
     * This method must compute the Fast Fourier Transform (FFT) of
     * one vector of complex numbers (encoded using the "Signal"
     * class). It must return a "Signal" object of the same size. The
     * complexity of this algorithm must be O(N * log(N)), where N is
     * the number of elements in the vector.
     *
     * N must be a power of 2. If it is not the case, an
     * "IllegalArgumentException" must be thrown.
     *
     * This is the same function as in homework "04-ComputeFourier".
     *
     * @param source The input signal in the time domain.
     * @return The output of the FFT, i.e. the Fourier spectrum.
     **/
    public static Signal computeFFT(Signal source) {
        int N = source.getLength();

        if ((N == 0) || (N ==1)){
            return source;
        }

        if (! (N > 0 && (N & (N - 1)) == 0)) {
            throw new IllegalArgumentException("N must be a power of 2.");
        }

        Signal even_signal = new Signal(N / 2);
        Signal odd_signal = new Signal(N / 2);

        for (int m = 0; m < N / 2; m++) {
            even_signal.setValue(m, source.getValue(m * 2));
            odd_signal.setValue(m, source.getValue(m * 2 + 1));
        }

        Signal even_fft = computeFFT(even_signal);
        Signal odd_fft = computeFFT(odd_signal);

        Signal result = new Signal(N);
        for (int k = 0; k < N / 2; k++) {
            double theta = -2 * Math.PI * k / N;
            ComplexNumber exp = exp_img(new ComplexNumber(0, theta));

            ComplexNumber evenValue = even_fft.getValue(k);
            ComplexNumber oddValue = multi_img(odd_fft.getValue(k), exp);

            result.setValue(k, add_img(evenValue, oddValue));
            result.setValue(k + N / 2, subs_img(evenValue, oddValue));
        }

        return result;
    }

    public static ComplexNumber add_img(ComplexNumber a, ComplexNumber b){
        double reel = a.getReal() + b.getReal();
        double img = a.getImag() + b.getImag();
        return new ComplexNumber(reel, img);
    }

    public static ComplexNumber subs_img(ComplexNumber a, ComplexNumber b){
        double reel = a.getReal() - b.getReal();
        double img = a.getImag() - b.getImag();
        return new ComplexNumber(reel, img);
    }

    public static ComplexNumber multi_img(ComplexNumber a, ComplexNumber b){
        double reel = (a.getReal()*b.getReal() - a.getImag()*b.getImag());
        double img = (a.getReal()*b.getImag() + a.getImag()*b.getReal());
        return new ComplexNumber(reel, img);
    }

    public static ComplexNumber exp_img(ComplexNumber a){
        double reel = (Math.cos(a.getImag()));
        double img = Math.sin(a.getImag());
        return new ComplexNumber(reel, img);
    }

    public static ComplexNumber conjugate(ComplexNumber a) {
        double reel = a.getReal();
        double img = - a.getImag();
        return new ComplexNumber(reel, img);
    }

    public static ComplexNumber divide(ComplexNumber z1, ComplexNumber z2) {
        double a = z1.getReal();
        double b = z1.getImag();
        double c = z2.getReal();
        double d = z2.getImag();

        // Denominator is (c^2 + d^2)
        double denominator = c * c + d * d;

        // If denominator is zero, we cannot divide (to avoid division by zero)
        if (denominator == 0) {
            throw new ArithmeticException("Cannot divide by zero (complex denominator).");
        }
        // Real part of the result
        double realPart = (a * c + b * d) / denominator;

        // Imaginary part of the result
        double imagPart = (b * c - a * d) / denominator;

        // Return the result as a new complex number
        return new ComplexNumber(realPart, imagPart);
    }



    /**
     * This method must compute the Inverse Fast Fourier Transform of
     * one vector of complex numbers (encoded using the "Signal"
     * class). It must return a "Signal" object of the same size. The
     * complexity of this algorithm must be O(N * log(N)), where N is
     * the number of elements in the vector.
     *
     * N must be a power of 2. If it is not the case, an
     * "IllegalArgumentException" must be thrown.
     *
     * @param source The input Fourier spectrum.
     * @return The output of the Inverse FFT, i.e. the signal in the time domain.
     **/
    public static Signal computeInverseFFT(Signal source) {
        int N = source.getLength();

        Signal result = new Signal(N);

        for (int i = 0; i < N; i++){
            result.setValue(i, conjugate(source.getValue(i)));
        }

        result = computeFFT(result);

        for (int i = 0; i < N; i++){
            result.setValue(i, divide(result.getValue(i), new ComplexNumber(N, 0)));
        }

        for (int i = 0; i < N; i++){
            result.setValue(i, conjugate(result.getValue(i)));
        }

        return result;

    }


    /**
     * Returns the frequency that is associated with item of index "k"
     * (i.e. "g[k]") in a discretized filter applicable to a signal of
     * length "N" whose sampling frequency is "Fs". Check out the last
     * slide of Session 5.
     *
     * @param k The index of interest.
     * @param N The length of the signal.
     * @param Fs The sampling frequency of the signal (i.e. "1/T").
     * @return The frequency. 
     **/
    public static double getFrequency(int k,
                                      int N,
                                      double Fs) {
        if( k >= N || k < 0){
            throw new IllegalArgumentException("Invalid input");
        }
        if (k <= N/2){
            return (k*Fs)/N;
        }
        return (k-N)*(Fs/N);
    }


    /**
     * Creates discretized low-pass, high-pass or pass-band filter
     * applicable to a signal of length "N" by sampling an ideal
     * filter.
     *
     * If neither "hasHighpass", nor "hasLowpass" is true, the filter
     * must have a gain of 100% everywhere.
     *
     * If both "hasHighpass" and "hasLowpass" are true, the filter
     * must have a gain of 100% inside the range
     * "[highpassCutoff,lowpassCutoff]", and 0% outside of this range.
     * If "highpassCutoff > lowpassCutoff", the gain is 0% everywhere.
     *
     * If "hasHighpass" (resp. "hasLowpass") is the only Boolean to be
     * "true", the function must create a high-pass (resp. low-pass)
     * filter whose cutoff frequency is "highpassCutoff"
     * (resp. "lowpassCutoff").
     *
     * To implement this function, make sure to use the function
     * "getFrequency()" above.
     *
     * @param N The length of the signal.
     * @param Fs The sampling frequency of the signal (i.e. "1/T").
     * @param hasHighpass Whether a high-pass filter is to be generated.
     * @param highpassCutoff Cutoff frequency for high-pass filtering.
     * Only makes sense if "hasHighpass" is "true".
     * @param hasLowpass Whether a low-pass filter is to be generated.
     * @param lowpassCutoff Cutoff frequency for low-pass filtering.
     * Only makes sense if "hasLowpass" is "true".
     * @return The filter in the frequency domain.
     **/
    public static Signal createFilter(int N,
                                      double Fs,
                                      boolean hasHighpass,
                                      double highpassCutoff,
                                      boolean hasLowpass,
                                      double lowpassCutoff) {
        Signal result = new Signal(N);

        if (!hasHighpass && !hasLowpass){
            for (int i = 0; i < N; i++){
                result.setValue(i, 1);
            }

            //gain = 100%
        } else if (hasHighpass && hasLowpass){
            if (highpassCutoff > lowpassCutoff){
                //gain = 0
                for (int i = 0; i < N; i++){
                    result.setValue(i, 0);
                }
            } else {
                for (int k = 0; k < N; k++){

                    if ((Math.abs(getFrequency(k, N, Fs)) <= lowpassCutoff) && (Math.abs(getFrequency(k, N, Fs)) >= highpassCutoff)) {
                        result.setValue(k, 1);
                    } else  {
                        result.setValue(k, 0);
                    }

                }

            }
            // gain = 100 in [highpass,lowpass] and 0 otherwise
        } else if (hasHighpass){

            for (int k = 0; k < N; k++){

                if (Math.abs(getFrequency(k, N, Fs)) > highpassCutoff) {
                    result.setValue(k, 1);
                } else {
                    result.setValue(k, 0);
                }

            }

        } else {
            for (int k = 0; k < N; k++){

                if (Math.abs(getFrequency(k, N, Fs)) < lowpassCutoff) {
                    result.setValue(k, 1);
                } else {
                    result.setValue(k, 0);
                }

            }
        }

        return result;
    }
    

    /**
     * Apply an ideal low-pass, high-pass or pass-band filter to one
     * channel in some EEG time series. The conventions for specifying
     * the filter using the arguments of the function are the same as
     * in function "createFilter()".
     *
     * The input signal must be padded with zeros until its length
     * corresponds to a power of 2. The output signal must be cropped
     * to the original length of the signal (i.e. the filtered items
     * corresponding to the padded zeros must be removed).
     *
     * @param timeSeries The EEG data.
     * @param channelIndex The index of the channel of interest.
     * @param hasHighpass Whether a high-pass filter is to be used.
     * @param highpassCutoff Cutoff frequency for high-pass filtering.
     * Only makes sense if "hasHighpass" is "true".
     * @param hasLowpass Whether a low-pass filter is to be used.
     * @param lowpassCutoff Cutoff frequency for low-pass filtering.
     * Only makes sense if "hasLowpass" is "true".
     * @return The filtered EEG channel.
     * @see #createFilter(int, double, boolean, double, boolean, double) 
     **/
    public static Signal filter(EDFTimeSeries timeSeries,
                                int channelIndex,
                                boolean hasHighpass,
                                double highpassCutoff,
                                boolean hasLowpass,
                                double lowpassCutoff) throws IOException {

        if (timeSeries == null) {
            throw new IllegalArgumentException("timeSeries is null");
        } else if (timeSeries.getNumberOfChannels() < channelIndex) {
            throw new IllegalArgumentException("timeSeries is null");
        } else {
            EDFTimeSeries.Channel channel = timeSeries.getChannel(channelIndex);

            int N = timeSeries.getNumberOfSamples(channelIndex);

            int new_N = N;
            while (!(new_N > 0 && (new_N & (new_N - 1)) == 0)) {
                new_N++;
            }
            Signal signal = new Signal(new_N);
            double frequency = timeSeries.getSamplingFrequency(channelIndex);
            Signal filter = createFilter(new_N, frequency, hasHighpass, highpassCutoff, hasLowpass, lowpassCutoff);

            for (int i = 0; i < N; i++) {
                signal.setValue(i, channel.getPhysicalValue(timeSeries.getDigitalValue(channelIndex, i)));
            }
            for (int j = N; j < new_N; j++) {
                signal.setValue(j, new ComplexNumber(0, 0));
            }

            signal = computeFFT(signal);

            for (int k = 0; k < new_N; k++) {
                signal.setValue(k, multi_img(filter.getValue(k), signal.getValue(k)));
            }

            signal = computeInverseFFT(signal);
            Signal final_signal = new Signal(N);
            for (int z = 0; z < N; z++) {
                final_signal.setValue(z, signal.getValue(z).getReal());
            }

            return final_signal;
        }
    }
}
