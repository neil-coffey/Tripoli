/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramBuilder;
import org.cirdles.tripoli.plots.linePlots.ComboPlotBuilder;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.plots.linePlots.MultiLinePlotBuilder;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.pow;
import static java.lang.StrictMath.exp;
import static org.cirdles.tripoli.utilities.comparators.SerializableIntegerComparator.SERIALIZABLE_COMPARATOR;

/**
 * @author James F. Bowring
 */
public enum SingleBlockDataModelPlot {
    ;

    public static PlotBuilder[][] analysisAndPlotting(
            SingleBlockDataSetRecord singleBlockDataSetRecord,
            List<EnsemblesStore.EnsembleRecord> ensembleRecordsList,
            SingleBlockModelRecord singleBlockInitialModelRecordInitial,
            AnalysisMethod analysisMethod) {
        List<IsotopicRatio> isotopicRatioList = analysisMethod.getTripoliRatiosList();

        /*
            %% Analysis and Plotting

            burn = 1000; % Number of models to discard
            ens_rat =[ensemble.lograt];

            % Calculate mean and st dev of ratios after burn in time
            ratmean = mean(ens_rat(:,burn:cnt),2);  % Log ratios
            ratstd = std(ens_rat(:,burn:cnt),[],2);

            BLmean = mean(ens_BL(:,burn:cnt),2);  % Baselines
            BLstd = std(ens_BL(:,burn:cnt),[],2);

            sigmean = mean(ens_sig(:,burn:cnt),2);   % Noise hyperparams
            sigstd = std(ens_sig(:,burn:cnt),[],2);

            DFmean = mean(ens_DF(:,burn:cnt),2);   % Daly-Far gain
            DFstd = std(ens_DF(:,burn:cnt),[],2);

         */
        int burn;// = 100;// 500;//1000;
        burn = 450;//Math.min(100, ensembleRecordsList.size() - 50);
        int countOfEnsemblesUsed = ensembleRecordsList.size() - burn;

        // log ratios
        double[][] ensembleSetOfLogRatios = new double[isotopicRatioList.size()][countOfEnsemblesUsed];
        double[][] ensembleRatios = new double[isotopicRatioList.size()][countOfEnsemblesUsed];
        double[] logRatioMean = new double[isotopicRatioList.size()];
        double[] logRatioStdDev = new double[isotopicRatioList.size()];
        DescriptiveStatistics descriptiveStatisticsLogRatios = new DescriptiveStatistics();
        for (int ratioIndex = 0; ratioIndex < isotopicRatioList.size(); ratioIndex++) {
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleSetOfLogRatios[ratioIndex][index - burn] = ensembleRecordsList.get(index).logRatios()[ratioIndex];
                descriptiveStatisticsLogRatios.addValue(ensembleSetOfLogRatios[ratioIndex][index - burn]);
                ensembleRatios[ratioIndex][index - burn] = exp(ensembleSetOfLogRatios[ratioIndex][index - burn]);
            }
            logRatioMean[ratioIndex] = descriptiveStatisticsLogRatios.getMean();
            logRatioStdDev[ratioIndex] = descriptiveStatisticsLogRatios.getStandardDeviation();
        }

        // baseLines
        int baselineSize = singleBlockInitialModelRecordInitial.faradayCount();
        double[][] ensembleBaselines = new double[baselineSize][countOfEnsemblesUsed];
        double[] baselinesMeans = new double[baselineSize];
        double[] baselinesStdDev = new double[baselineSize];

        for (int row = 0; row < baselineSize; row++) {
            DescriptiveStatistics descriptiveStatisticsBaselines = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                // todo: fix magic number
                ensembleBaselines[row][index - burn] = ensembleRecordsList.get(index).baseLine()[row];//TODO: Decide / 6.24e7 * 1e6;
                descriptiveStatisticsBaselines.addValue(ensembleBaselines[row][index - burn]);
            }
            baselinesMeans[row] = descriptiveStatisticsBaselines.getMean();
            baselinesStdDev[row] = descriptiveStatisticsBaselines.getStandardDeviation();
        }

        // dalyFaraday gains
        double[] ensembleDalyFaradayGain = new double[countOfEnsemblesUsed];
        DescriptiveStatistics descriptiveStatisticsDalyFaradayGain = new DescriptiveStatistics();
        for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
            ensembleDalyFaradayGain[index - burn] = ensembleRecordsList.get(index).dfGain();
            descriptiveStatisticsDalyFaradayGain.addValue(ensembleDalyFaradayGain[index - burn]);
        }
        double dalyFaradayGainMean = descriptiveStatisticsDalyFaradayGain.getMean();
        double dalyFaradayGainStdDev = descriptiveStatisticsDalyFaradayGain.getStandardDeviation();

        // signal noise
        int faradayCount = singleBlockInitialModelRecordInitial.faradayCount();
        double[][] ensembleSignalnoise = new double[faradayCount][countOfEnsemblesUsed];
        double[] signalNoiseMeans = new double[faradayCount];
        double[] signalNoiseStdDev = new double[faradayCount];

        for (int row = 0; row < faradayCount; row++) {
            DescriptiveStatistics descriptiveStatisticsSignalNoise = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleSignalnoise[row][index - burn] = ensembleRecordsList.get(index).signalNoise()[row];
                descriptiveStatisticsSignalNoise.addValue(ensembleSignalnoise[row][index - burn]);
            }
            signalNoiseMeans[row] = descriptiveStatisticsSignalNoise.getMean();
            signalNoiseStdDev[row] = descriptiveStatisticsSignalNoise.getStandardDeviation();
        }

        /*
            for m=1:d0.Nblock
                for n = 1:cnt;
                    ens_I{m}(:,n) =[ensemble(n).I{m}];
                end
                Imean{m} = mean(ens_I{m}(:,burn:cnt),2);
                Istd{m} = std(ens_I{m}(:,burn:cnt),[],2);
            end
         */

        // Intensity
        int knotsCount = singleBlockInitialModelRecordInitial.I0().length;
        double[][] ensembleIntensity = new double[knotsCount][countOfEnsemblesUsed];
        double[] intensityMeans = new double[knotsCount];
        double[] intensityStdDevs = new double[knotsCount];

        for (int knotIndex = 0; knotIndex < knotsCount; knotIndex++) {
            DescriptiveStatistics descriptiveStatisticsIntensity = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleIntensity[knotIndex][index - burn] = ensembleRecordsList.get(index).intensities()[knotIndex];
                descriptiveStatisticsIntensity.addValue(ensembleIntensity[knotIndex][index - burn]);
            }
            intensityMeans[knotIndex] = descriptiveStatisticsIntensity.getMean();
            intensityStdDevs[knotIndex] = descriptiveStatisticsIntensity.getStandardDeviation();
        }

        // calculate blockIntensities means for plotting
        double[][] yDataIntensityMeans = new double[1][];
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        MatrixStore<Double> intensityMeansMatrix = storeFactory.columns(intensityMeans);
        MatrixStore<Double> yDataMatrix = singleBlockDataSetRecord.blockKnotInterpolationStore().multiply(intensityMeansMatrix).multiply(1.0 / dalyFaradayGainMean);//(1.0 / (dalyFaradayGainMean * 6.24e7)) * 1e6);
        yDataIntensityMeans[0] = yDataMatrix.toRawCopy1D();
        double[] xDataIntensityMeans = new double[singleBlockDataSetRecord.blockKnotInterpolationStore().getRowDim()];
        for (int i = 0; i < xDataIntensityMeans.length; i++) {
            xDataIntensityMeans[i] = i;
        }

        // visualization - Ensembles tab
        PlotBuilder[][] plotBuilders = new PlotBuilder[15][1];

        plotBuilders[0] = new PlotBuilder[ensembleRatios.length];
        for (int i = 0; i < ensembleRatios.length; i++) {
            plotBuilders[0][i] = HistogramBuilder.initializeHistogram(ensembleRatios[i],
                    25, new String[]{isotopicRatioList.get(i).prettyPrint()}, "Ratios", "Frequency");
        }

        plotBuilders[1] = new PlotBuilder[ensembleBaselines.length];
        List<Detector> faradayDetectorsUsed = analysisMethod.getSequenceTable().findFaradayDetectorsUsed();
        for (int i = 0; i < ensembleBaselines.length; i++) {
            plotBuilders[1][i] = HistogramBuilder.initializeHistogram(ensembleBaselines[i],
                    25, new String[]{faradayDetectorsUsed.get(i).getDetectorName() + " Baseline"}, "Baseline Counts", "Frequency");
        }

        plotBuilders[2][0] = HistogramBuilder.initializeHistogram(ensembleDalyFaradayGain,
                25, new String[]{"Daly/Faraday Gain"}, "Gain", "Frequency");

        plotBuilders[3] = new PlotBuilder[ensembleSignalnoise.length];
        for (int i = 0; i < ensembleSignalnoise.length; i++) {
            plotBuilders[3][i] = HistogramBuilder.initializeHistogram(ensembleSignalnoise[i],
                    25, new String[]{faradayDetectorsUsed.get(i).getDetectorName() + " Signal Noise"}, "Noise hyperparameter", "Frequency");
        }

        plotBuilders[4][0] = MultiLinePlotBuilder.initializeLinePlot(
                xDataIntensityMeans, yDataIntensityMeans, new String[]{"Mean Intensity"}, "Time Index", "Intensity (counts)");

        // visualization converge ratio and others tabs
        double[][] convergeIntensities = new double[ensembleRecordsList.get(0).intensities().length][ensembleRecordsList.size()];
        double[] convergeNoiseFaradayL1 = new double[ensembleRecordsList.size()];
        double[] convergeNoiseFaradayH1 = new double[ensembleRecordsList.size()];
        for (int index = 0; index < ensembleRecordsList.size(); index++) {
            for (int intensityIndex = 0; intensityIndex < convergeIntensities.length; intensityIndex++) {
                // todo: fix this block indexing issue
                convergeIntensities[intensityIndex][index] = ensembleRecordsList.get(index).intensities()[intensityIndex];
            }
//            convergeNoiseFaradayL1[index] = ensembleRecordsList.get(index).signalNoiseSigma()[0];
//            convergeNoiseFaradayH1[index] = ensembleRecordsList.get(index).signalNoiseSigma()[1];
//            xDataConvergeSavedIterations[index] = index + 1;
        }

        // new converge plots
        double[][] convergeSetOfLogRatios = new double[isotopicRatioList.size()][ensembleRecordsList.size()];
        double[][] convergeSetOfBaselines = new double[baselineSize][ensembleRecordsList.size()];
        double[][] convergeSetOfFaradayNoise = new double[baselineSize][ensembleRecordsList.size()];
        double[] convergeErrWeightedMisfit = new double[ensembleRecordsList.size()];
        double[] convergeErrRawMisfit = new double[ensembleRecordsList.size()];
        double[] xDataConvergeSavedIterations = new double[ensembleRecordsList.size()];
        for (int ensembleIndex = 0; ensembleIndex < ensembleRecordsList.size(); ensembleIndex++) {
            for (int ratioIndex = 0; ratioIndex < isotopicRatioList.size(); ratioIndex++) {
                convergeSetOfLogRatios[ratioIndex][ensembleIndex] = ensembleRecordsList.get(ensembleIndex).logRatios()[ratioIndex];
            }
            for (int faradayIndex = 0; faradayIndex < baselineSize; faradayIndex++) {
                convergeSetOfBaselines[faradayIndex][ensembleIndex] = ensembleRecordsList.get(ensembleIndex).baseLine()[faradayIndex];
                convergeSetOfFaradayNoise[faradayIndex][ensembleIndex] = ensembleRecordsList.get(ensembleIndex).signalNoise()[faradayIndex];
            }
            convergeErrWeightedMisfit[ensembleIndex] = StrictMath.sqrt(ensembleRecordsList.get(ensembleIndex).errorWeighted());
            convergeErrRawMisfit[ensembleIndex] = StrictMath.sqrt(ensembleRecordsList.get(ensembleIndex).errorUnWeighted());

            xDataConvergeSavedIterations[ensembleIndex] = ensembleIndex + 1;
        }

        plotBuilders[5] = new PlotBuilder[convergeSetOfLogRatios.length];
        for (int i = 0; i < convergeSetOfLogRatios.length; i++) {
            plotBuilders[5][i] = LinePlotBuilder.initializeLinePlot(
                    xDataConvergeSavedIterations, convergeSetOfLogRatios[i],
                    new String[]{isotopicRatioList.get(i).prettyPrint()}, "Saved iterations", "Log Ratio");
        }

        plotBuilders[6] = new PlotBuilder[convergeSetOfBaselines.length];
        for (int i = 0; i < convergeSetOfBaselines.length; i++) {
            plotBuilders[6][i] = LinePlotBuilder.initializeLinePlot(
                    xDataConvergeSavedIterations, convergeSetOfBaselines[i],
                    new String[]{faradayDetectorsUsed.get(i).getDetectorName() + " Baseline"}, "Saved iterations", "Baseline Counts");
        }

        plotBuilders[11] = new PlotBuilder[convergeSetOfFaradayNoise.length];
        for (int i = 0; i < convergeSetOfFaradayNoise.length; i++) {
            plotBuilders[11][i] = LinePlotBuilder.initializeLinePlot(
                    xDataConvergeSavedIterations, convergeSetOfFaradayNoise[i],
                    new String[]{faradayDetectorsUsed.get(i).getDetectorName() + " Noise"}, "Saved iterations", "Noise");
        }

        plotBuilders[8][0] = LinePlotBuilder.initializeLinePlot(xDataConvergeSavedIterations, convergeErrWeightedMisfit, new String[]{"Converge Weighted Misfit"}, "Saved iterations", "Weighted Misfit");

        plotBuilders[9][0] = LinePlotBuilder.initializeLinePlot(xDataConvergeSavedIterations, convergeErrRawMisfit, new String[]{"Converge Raw Misfit"}, "Saved iterations", "Raw Misfit");


        plotBuilders[10][0] = MultiLinePlotBuilder.initializeLinePlot(xDataConvergeSavedIterations, convergeIntensities, new String[]{"Converge Intensity"}, "", "");

        plotBuilders[11][0] = LinePlotBuilder.initializeLinePlot(xDataConvergeSavedIterations, convergeNoiseFaradayL1, new String[]{"Converge Noise Faraday L1"},"","");
        plotBuilders[12][0] = LinePlotBuilder.initializeLinePlot(xDataConvergeSavedIterations, convergeNoiseFaradayH1, new String[]{"Converge Noise Faraday H1"},"","");


        // visualization data fit

        double[] data = singleBlockInitialModelRecordInitial.dataArray();
        double[] dataWithNoBaseline = singleBlockInitialModelRecordInitial.dataWithNoBaselineArray();

        double[] xSig = singleBlockInitialModelRecordInitial.signalNoiseSigma();
        int[] detectorIndicesForRawDataColumn = singleBlockDataSetRecord.blockDetectorOrdinalIndicesArray();
        double[] dataCountsModelOneSigma = new double[detectorIndicesForRawDataColumn.length];
        for (int row = 0; row < detectorIndicesForRawDataColumn.length; row++) {
            dataCountsModelOneSigma[row]
                    = StrictMath.sqrt(StrictMath.pow(xSig[(int) detectorIndicesForRawDataColumn[row] - 1], 2)
                    + xSig[xSig.length - 1] * dataWithNoBaseline[row]);
        }

        int plottingStep = 10;
        double[] dataOriginalCounts = singleBlockInitialModelRecordInitial.dataArray();
        double[] xDataIndex = new double[dataOriginalCounts.length / plottingStep];
        double[] yDataCounts = new double[dataOriginalCounts.length / plottingStep];
        double[] yDataModelCounts = new double[dataOriginalCounts.length / plottingStep];

        double[] yDataResiduals = new double[dataOriginalCounts.length / plottingStep];
        double[] yDataSigmas = new double[dataOriginalCounts.length / plottingStep];

        for (int i = 0; i < dataOriginalCounts.length / plottingStep; i++) {
            xDataIndex[i] = i * plottingStep;
            yDataCounts[i] = dataOriginalCounts[i * plottingStep];
            yDataModelCounts[i] = data[i * plottingStep];
            yDataResiduals[i] = dataOriginalCounts[i * plottingStep] - data[i * plottingStep];
            yDataSigmas[i] = dataCountsModelOneSigma[i * plottingStep];
        }
        plotBuilders[13][0] = ComboPlotBuilder.initializeLinePlot(xDataIndex, yDataCounts, yDataModelCounts, new String[]{"Observed Data"});

        plotBuilders[14][0] = ComboPlotBuilder.initializeLinePlotWithOneSigma(xDataIndex, yDataResiduals, yDataSigmas, new String[]{"Residual Data"});


        // todo: missing additional elements of signalNoiseSigma (i.e., 0,11,11)
        System.err.println(logRatioMean + "         " + logRatioStdDev);
        System.err.println(baselinesMeans[0] + "         " + baselinesMeans[1] + "    " + baselinesStdDev[0] + "     " + baselinesStdDev[1]);
        System.err.println(dalyFaradayGainMean + "    " + dalyFaradayGainStdDev);
        System.err.println(signalNoiseMeans[0] + "         " + signalNoiseMeans[1] + "    " + signalNoiseStdDev[0] + "     " + signalNoiseStdDev[1]);


        return plotBuilders;
    }


}