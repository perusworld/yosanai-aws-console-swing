/**
 *
 * This is the MIT License
 * http://www.opensource.org/licenses/mit-license.php
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package com.yosanai.java.aws.console;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.InstanceType;

/**
 * @author Saravana Perumal Shanmugam
 * 
 */
public interface AWSConnectionProvider {
    /**
     * 
     */
    public static final String AWS_SECRET = "aws.secret";

    /**
     * 
     */
    public static final String AWS_KEY = "aws.key";

    public AmazonEC2 getConnection();

    public void updateEC2Config(boolean reuseExisting, Configuration config) throws Exception;

    public void startInstances(String... instanceIds) throws Exception;

    public void stopInstances(String... instanceIds) throws Exception;

    public void terminateInstances(String... instanceIds) throws Exception;

    public void setApiTermination(boolean enable, String... instanceIds) throws Exception;

    public boolean getApiTermination(String instanceId) throws Exception;

    public String getInstanceDetails(String property, String separator, String... instanceIds) throws Exception;

    public Collection<String> getSecurityGroups() throws Exception;

    public Collection<String> getKeyPairNames() throws Exception;

    public void launchInstance(String amiId, InstanceType instanceType, int instanceCount, String keyName,
            Collection<String> securityGroups, boolean terminateViaAPI, Map<String, String> tags) throws Exception;

}
