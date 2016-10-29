/*******************************************************************************
 * Copyright 2016
 * Language Technology Lab
 * University of Duisburg-Essen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.unidue.ltl.flextag.core.reports.crf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;

public class TtCrfKnownUnknownWordAccuracyReport
    extends TtAbstractKnownUnknownWordAccuracyReport
{
    {
        TCMachineLearningAdapter adapter = CRFSuiteAdapter.getInstance();
        featureFile = adapter.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        predictionFile = adapter.getFrameworkFilename(AdapterNameEntries.predictionsFile);
    }
    
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

       super.execute();

        File train = buildFileLocation(store, trainContextId,
                TEST_TASK_OUTPUT_KEY + "/" + featureFile);
        List<String> trainVocab = extractVocab(train);

        File test = buildFileLocation(store, testContextId,
                TEST_TASK_OUTPUT_KEY + "/" + featureFile);
        List<String> testVocab = extractVocab(test);

        File p = buildFileLocation(store, predictionContextId, predictionFile);
        outputFolder = p.getParentFile();
        List<String> pred = readPredictions(p);
        evaluate(trainVocab, testVocab, pred);
        writeResults();
    }

    @Override
    protected List<String> readPredictions(File p)
            throws IOException
        {
            List<String> pre = new ArrayList<>();
            List<String> readLines = FileUtils.readLines(p);
            int i = 0;
            for (String r : readLines) {
                if (r.isEmpty()) {
                    continue;
                }
                if (r.startsWith("#") && i == 0) {
                    i++;
                    continue;
                }
                pre.add(r);
            }

            return pre;
        }

    protected String extractUnit(String next)
    {
        int start = next.indexOf(ID_FEATURE_NAME);
        int end = next.indexOf("\t", start);
        if (end == -1) {
            end = next.length();
        }
        start = next.lastIndexOf("_", end);

        String word = next.substring(start + 1, end);

        return word;
    }

    @Override
    protected String[] splitPredictions(String string)
    {
        return string.split("\t");
    }
}