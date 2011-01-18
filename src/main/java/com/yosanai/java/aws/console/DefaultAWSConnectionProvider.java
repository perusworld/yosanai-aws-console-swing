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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

/**
 * @author Saravana Perumal Shanmugam
 * 
 */
public class DefaultAWSConnectionProvider implements AWSConnectionProvider {

    /**
     * 
     */
    public static final String DISABLE_API_TERMINATION = "disableApiTermination";

    /**
     * 
     */
    public static final String STATE_RUNNING = "running";

    /**
     * 
     */
    public static final String STATE_STOPPED = "stopped";

    /**
     * 
     */
    public static final String STATE_PENDING = "pending";

    /**
     * 
     */
    public static final String STATE_SHUTTING_DOWN = "shutting-down";

    /**
     * 
     */
    public static final String STATE_TERMINATED = "terminated";

    protected AmazonEC2 amazonEC2;

    protected Object lock = new Object();

    /*
     * (non-Jsdoc)
     * 
     * @see com.yosanai.java.aws.console.AWSConnectionProvider#getConnection()
     */
    @Override
    public AmazonEC2 getConnection() {
        AmazonEC2 ret = null;
        synchronized (lock) {
            ret = amazonEC2;
        }
        return ret;
    }

    /*
     * (non-Jsdoc)
     * 
     * @see
     * com.yosanai.java.aws.console.AWSConnectionProvider#updateEC2Config(boolean
     * , org.apache.commons.configuration.Configuration)
     */
    @Override
    public void updateEC2Config(boolean reuseExisting, Configuration config) throws Exception {
        if (StringUtils.isNotBlank(config.getString(AWS_KEY))) {
            if (null == amazonEC2 || !reuseExisting) {
                synchronized (lock) {
                    if (null != amazonEC2) {
                        amazonEC2.shutdown();
                        amazonEC2 = null;
                    }
                    amazonEC2 = new AmazonEC2Client(new BasicAWSCredentials(config.getString(AWS_KEY, ""),
                            config.getString(AWS_SECRET, "")));
                    amazonEC2.describeInstances();
                }
            }
        }
    }

    protected List<String> getInstances(String state, boolean include, String... instanceIds) {
        List<String> ret = new ArrayList<String>();
        if (null == state) {
            if (null != instanceIds) {
                ret.addAll(Arrays.asList(instanceIds));
            }
        } else {
            DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
            if (null != instanceIds) {
                describeInstancesRequest.setInstanceIds(Arrays.asList(instanceIds));
            }
            DescribeInstancesResult describeInstancesResult = getConnection().describeInstances(
                    describeInstancesRequest);
            for (Reservation reservation : describeInstancesResult.getReservations()) {
                for (Instance instance : reservation.getInstances()) {
                    if (include) {
                        if (state.equals(instance.getState().getName())) {
                            ret.add(instance.getInstanceId());
                        }
                    } else if (!state.equals(instance.getState())) {
                        ret.add(instance.getInstanceId());
                    }
                }
            }
        }
        return ret;
    }

    /*
     * (non-Jsdoc)
     * 
     * @see
     * com.yosanai.java.aws.console.AWSConnectionProvider#startInstances(java
     * .lang.String[])
     */
    @Override
    public void startInstances(String... instanceIds) throws Exception {
        List<String> instanceIdsFiltered = getInstances(STATE_STOPPED, true, instanceIds);
        if (!instanceIdsFiltered.isEmpty()) {
            StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
            startInstancesRequest.setInstanceIds(instanceIdsFiltered);
            getConnection().startInstances(startInstancesRequest);
        }
    }

    /*
     * (non-Jsdoc)
     * 
     * @see
     * com.yosanai.java.aws.console.AWSConnectionProvider#stopInstances(java
     * .lang.String[])
     */
    @Override
    public void stopInstances(String... instanceIds) throws Exception {
        List<String> instanceIdsFiltered = getInstances(STATE_RUNNING, true, instanceIds);
        if (!instanceIdsFiltered.isEmpty()) {
            StopInstancesRequest stopInstancesRequest = new StopInstancesRequest();
            stopInstancesRequest.setInstanceIds(instanceIdsFiltered);
            getConnection().stopInstances(stopInstancesRequest);
        }
    }

    /*
     * (non-Jsdoc)
     * 
     * @see
     * com.yosanai.java.aws.console.AWSConnectionProvider#terminateInstances
     * (java.lang.String[])
     */
    @Override
    public void terminateInstances(String... instanceIds) throws Exception {
        List<String> instanceIdsFiltered = getInstances(STATE_TERMINATED, false, instanceIds);
        if (!instanceIdsFiltered.isEmpty()) {
            TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
            terminateInstancesRequest.setInstanceIds(instanceIdsFiltered);
            getConnection().terminateInstances(terminateInstancesRequest);
        }
    }

    /*
     * (non-Jsdoc)
     * 
     * @see
     * com.yosanai.java.aws.console.AWSConnectionProvider#setApiTermination(
     * boolean, java.lang.String[])
     */
    @Override
    public void setApiTermination(boolean enable, String... instanceIds) throws Exception {
        if (null != instanceIds) {
            List<String> instances = Arrays.asList(instanceIds);
            if (!instances.isEmpty()) {
                for (String instance : instances) {
                    ModifyInstanceAttributeRequest modifyInstanceAttributeRequest = new ModifyInstanceAttributeRequest();
                    modifyInstanceAttributeRequest.setInstanceId(instance);
                    modifyInstanceAttributeRequest.setAttribute(DISABLE_API_TERMINATION);
                    modifyInstanceAttributeRequest.setValue("" + !enable);
                    getConnection().modifyInstanceAttribute(modifyInstanceAttributeRequest);
                }
            }
        }
    }

    /*
     * (non-Jsdoc)
     * 
     * @see
     * com.yosanai.java.aws.console.AWSConnectionProvider#getApiTermination(
     * java.lang.String)
     */
    @Override
    public boolean getApiTermination(String instanceId) throws Exception {
        boolean ret = false;
        if (StringUtils.isNotBlank(instanceId)) {
            DescribeInstanceAttributeResult describeInstanceAttributeResult = getConnection()
                    .describeInstanceAttribute(
                            new DescribeInstanceAttributeRequest(instanceId, DISABLE_API_TERMINATION));
            ret = !describeInstanceAttributeResult.getInstanceAttribute().getDisableApiTermination();
        }
        return ret;
    }

    protected String getInstanceDetail(String property, Instance instance) throws Exception {
        String ret = null;
        BeanWrapper beanWrapper = new BeanWrapperImpl(instance);
        Object value = beanWrapper.getPropertyValue(property);
        if (null != value) {
            ret = value.toString();
        }
        return ret;
    }

    /*
     * (non-Jsdoc)
     * 
     * @see
     * com.yosanai.java.aws.console.AWSConnectionProvider#getInstanceDetails
     * (java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public String getInstanceDetails(String property, String separator, String... instanceIds) throws Exception {
        StringBuilder ret = new StringBuilder();
        List<String> instances = getInstances(null, false, instanceIds);
        if (!instances.isEmpty()) {
            HashSet<String> instanceIdsSet = new HashSet<String>(instances);
            DescribeInstancesResult result = getConnection().describeInstances();
            for (Reservation reservation : result.getReservations()) {
                for (Instance instance : reservation.getInstances()) {
                    if (instanceIdsSet.contains(instance.getInstanceId())) {
                        String value = getInstanceDetail(property, instance);
                        if (StringUtils.isNotBlank(value)) {
                            if (0 < ret.length()) {
                                ret.append(separator);
                            }
                            ret.append(value);
                        }
                    }
                }
            }
        }
        return ret.toString();
    }
}
