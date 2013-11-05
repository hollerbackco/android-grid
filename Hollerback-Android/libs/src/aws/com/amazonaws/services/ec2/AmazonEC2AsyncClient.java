/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * 
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.services.ec2;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import com.amazonaws.services.ec2.model.*;


/**
 * Asynchronous client for accessing AmazonEC2.
 * All asynchronous calls made using this client are non-blocking. Callers could either
 * process the result and handle the exceptions in the worker thread by providing a callback handler
 * when making the call, or use the returned Future object to check the result of the call in the calling thread.
 * Amazon EC2 <p>
 * Amazon Elastic Compute Cloud (Amazon EC2) is a web service that provides resizable compute capacity in the cloud. It is designed to make web-scale
 * computing easier for developers.
 * </p>
 * <p>
 * Amazon EC2's simple web service interface allows you to obtain and configure capacity with minimal friction. It provides you with complete control of
 * your computing resources and lets you run on Amazon's proven computing environment. Amazon EC2 reduces the time required to obtain and boot new server
 * instances to minutes, allowing you to quickly scale capacity, both up and down, as your computing requirements change. Amazon EC2 changes the
 * economics of computing by allowing you to pay only for capacity that you actually use. Amazon EC2 provides developers the tools to build failure
 * resilient applications and isolate themselves from common failure scenarios.
 * </p>
 * <p>
 * Visit <a href="http://aws.amazon.com/ec2/"> http://aws.amazon.com/ec2/ </a> for more information.
 * </p>
 * <p>
 * See our blog to learn more about {@see <a href="http://mobile.awsblog.com/post/Tx31X75XISXHRH8/Managing-Credentials-in-Mobile-Applications">Managing Credentials in Mobile Applications</a>.}
 * </p>
 */
public class AmazonEC2AsyncClient extends AmazonEC2Client
        implements AmazonEC2Async {

    /**
     * Executor service for executing asynchronous requests.
     */
    private ExecutorService executorService;


    /**
     * Constructs a new asynchronous client to invoke service methods on
     * AmazonEC2.  A credentials provider chain will be used
     * that searches for credentials in this order:
     * <ul>
     *  <li> Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_KEY </li>
     *  <li> Java System Properties - aws.accessKeyId and aws.secretKey </li>
     *  <li> Instance profile credentials delivered through the Amazon EC2 metadata service </li>
     * </ul>
     *
     * <p>
     * All service calls made using this new client object are blocking, and will not
     * return until the service call completes.
     *
     * @see DefaultAWSCredentialsProviderChain
     */
    public AmazonEC2AsyncClient() {
        this(new DefaultAWSCredentialsProviderChain());
    }

    /**
     * Constructs a new asynchronous client to invoke service methods on
     * AmazonEC2.  A credentials provider chain will be used
     * that searches for credentials in this order:
     * <ul>
     *  <li> Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_KEY </li>
     *  <li> Java System Properties - aws.accessKeyId and aws.secretKey </li>
     *  <li> Instance profile credentials delivered through the Amazon EC2 metadata service </li>
     * </ul>
     *
     * <p>
     * All service calls made using this new client object are blocking, and will not
     * return until the service call completes.
     *
     * @param clientConfiguration The client configuration options controlling how this
     *                       client connects to AmazonEC2
     *                       (ex: proxy settings, retry counts, etc.).
     *
     * @see DefaultAWSCredentialsProviderChain
     */
    public AmazonEC2AsyncClient(ClientConfiguration clientConfiguration) {
        this(new DefaultAWSCredentialsProviderChain(), clientConfiguration, Executors.newCachedThreadPool());
    }

    /**
     * Constructs a new asynchronous client to invoke service methods on
     * AmazonEC2 using the specified AWS account credentials.
     * Default client settings will be used, and a default cached thread pool will be
     * created for executing the asynchronous tasks.
     *
     * <p>
     * All calls made using this new client object are non-blocking, and will immediately
     * return a Java Future object that the caller can later check to see if the service
     * call has actually completed.
     *
     * @param awsCredentials The AWS credentials (access key ID and secret key) to use
     *                       when authenticating with AWS services.
     */
    public AmazonEC2AsyncClient(AWSCredentials awsCredentials) {
        this(awsCredentials, Executors.newCachedThreadPool());
    }

    /**
     * Constructs a new asynchronous client to invoke service methods on
     * AmazonEC2 using the specified AWS account credentials
     * and executor service.  Default client settings will be used.
     *
     * <p>
     * All calls made using this new client object are non-blocking, and will immediately
     * return a Java Future object that the caller can later check to see if the service
     * call has actually completed.
     *
     * @param awsCredentials
     *            The AWS credentials (access key ID and secret key) to use
     *            when authenticating with AWS services.
     * @param executorService
     *            The executor service by which all asynchronous requests will
     *            be executed.
     */
    public AmazonEC2AsyncClient(AWSCredentials awsCredentials, ExecutorService executorService) {
        super(awsCredentials);
        this.executorService = executorService;
    }

    /**
     * Constructs a new asynchronous client to invoke service methods on
     * AmazonEC2 using the specified AWS account credentials,
     * executor service, and client configuration options.
     *
     * <p>
     * All calls made using this new client object are non-blocking, and will immediately
     * return a Java Future object that the caller can later check to see if the service
     * call has actually completed.
     *
     * @param awsCredentials
     *            The AWS credentials (access key ID and secret key) to use
     *            when authenticating with AWS services.
     * @param clientConfiguration
     *            Client configuration options (ex: max retry limit, proxy
     *            settings, etc).
     * @param executorService
     *            The executor service by which all asynchronous requests will
     *            be executed.
     */
    public AmazonEC2AsyncClient(AWSCredentials awsCredentials,
                ClientConfiguration clientConfiguration, ExecutorService executorService) {
        super(awsCredentials, clientConfiguration);
        this.executorService = executorService;
    }

    /**
     * Constructs a new asynchronous client to invoke service methods on
     * AmazonEC2 using the specified AWS account credentials provider.
     * Default client settings will be used, and a default cached thread pool will be
     * created for executing the asynchronous tasks.
     *
     * <p>
     * All calls made using this new client object are non-blocking, and will immediately
     * return a Java Future object that the caller can later check to see if the service
     * call has actually completed.
     *
     * @param awsCredentialsProvider
     *            The AWS credentials provider which will provide credentials
     *            to authenticate requests with AWS services.
     */
    public AmazonEC2AsyncClient(AWSCredentialsProvider awsCredentialsProvider) {
        this(awsCredentialsProvider, Executors.newCachedThreadPool());
    }

    /**
     * Constructs a new asynchronous client to invoke service methods on
     * AmazonEC2 using the specified AWS account credentials provider
     * and executor service.  Default client settings will be used.
     *
     * <p>
     * All calls made using this new client object are non-blocking, and will immediately
     * return a Java Future object that the caller can later check to see if the service
     * call has actually completed.
     *
     * @param awsCredentialsProvider
     *            The AWS credentials provider which will provide credentials
     *            to authenticate requests with AWS services.
     * @param executorService
     *            The executor service by which all asynchronous requests will
     *            be executed.
     */
    public AmazonEC2AsyncClient(AWSCredentialsProvider awsCredentialsProvider, ExecutorService executorService) {
        this(awsCredentialsProvider, new ClientConfiguration(), executorService);
    }

    /**
     * Constructs a new asynchronous client to invoke service methods on
     * AmazonEC2 using the specified AWS account credentials
     * provider and client configuration options.
     *
     * <p>
     * All calls made using this new client object are non-blocking, and will immediately
     * return a Java Future object that the caller can later check to see if the service
     * call has actually completed.
     *
     * @param awsCredentialsProvider
     *            The AWS credentials provider which will provide credentials
     *            to authenticate requests with AWS services.
     * @param clientConfiguration
     *            Client configuration options (ex: max retry limit, proxy
     *            settings, etc).
     */
    public AmazonEC2AsyncClient(AWSCredentialsProvider awsCredentialsProvider,
                ClientConfiguration clientConfiguration) {
        this(awsCredentialsProvider, clientConfiguration, Executors.newCachedThreadPool());
    }

    /**
     * Constructs a new asynchronous client to invoke service methods on
     * AmazonEC2 using the specified AWS account credentials
     * provider, executor service, and client configuration options.
     *
     * <p>
     * All calls made using this new client object are non-blocking, and will immediately
     * return a Java Future object that the caller can later check to see if the service
     * call has actually completed.
     *
     * @param awsCredentialsProvider
     *            The AWS credentials provider which will provide credentials
     *            to authenticate requests with AWS services.
     * @param clientConfiguration
     *            Client configuration options (ex: max retry limit, proxy
     *            settings, etc).
     * @param executorService
     *            The executor service by which all asynchronous requests will
     *            be executed.
     */
    public AmazonEC2AsyncClient(AWSCredentialsProvider awsCredentialsProvider,
                ClientConfiguration clientConfiguration, ExecutorService executorService) {
        super(awsCredentialsProvider, clientConfiguration);
        this.executorService = executorService;
    }


    /**
     * Returns the executor service used by this async client to execute
     * requests.
     *
     * @return The executor service used by this async client to execute
     *         requests.
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Shuts down the client, releasing all managed resources. This includes
     * forcibly terminating all pending asynchronous service calls. Clients who
     * wish to give pending asynchronous service calls time to complete should
     * call getExecutorService().shutdown() prior to calling this method.
     */
    @Override
    public void shutdown() {
        super.shutdown();
        executorService.shutdownNow();
    }
            
    /**
     * <p>
     * The RebootInstances operation requests a reboot of one or more
     * instances. This operation is asynchronous; it only queues a request to
     * reboot the specified instance(s). The operation will succeed if the
     * instances are valid and belong to the user. Requests to reboot
     * terminated instances are ignored.
     * </p>
     *
     * @param rebootInstancesRequest Container for the necessary parameters
     *           to execute the RebootInstances operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         RebootInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> rebootInstancesAsync(final RebootInstancesRequest rebootInstancesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                rebootInstances(rebootInstancesRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * The RebootInstances operation requests a reboot of one or more
     * instances. This operation is asynchronous; it only queues a request to
     * reboot the specified instance(s). The operation will succeed if the
     * instances are valid and belong to the user. Requests to reboot
     * terminated instances are ignored.
     * </p>
     *
     * @param rebootInstancesRequest Container for the necessary parameters
     *           to execute the RebootInstances operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         RebootInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> rebootInstancesAsync(
            final RebootInstancesRequest rebootInstancesRequest,
            final AsyncHandler<RebootInstancesRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		rebootInstances(rebootInstancesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(rebootInstancesRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeReservedInstances operation describes Reserved Instances
     * that were purchased for use with your account.
     * </p>
     *
     * @param describeReservedInstancesRequest Container for the necessary
     *           parameters to execute the DescribeReservedInstances operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeReservedInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeReservedInstancesResult> describeReservedInstancesAsync(final DescribeReservedInstancesRequest describeReservedInstancesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeReservedInstancesResult>() {
            public DescribeReservedInstancesResult call() throws Exception {
                return describeReservedInstances(describeReservedInstancesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeReservedInstances operation describes Reserved Instances
     * that were purchased for use with your account.
     * </p>
     *
     * @param describeReservedInstancesRequest Container for the necessary
     *           parameters to execute the DescribeReservedInstances operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeReservedInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeReservedInstancesResult> describeReservedInstancesAsync(
            final DescribeReservedInstancesRequest describeReservedInstancesRequest,
            final AsyncHandler<DescribeReservedInstancesRequest, DescribeReservedInstancesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeReservedInstancesResult>() {
            public DescribeReservedInstancesResult call() throws Exception {
            	DescribeReservedInstancesResult result;
                try {
            		result = describeReservedInstances(describeReservedInstancesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeReservedInstancesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeAvailabilityZones operation describes availability zones
     * that are currently available to the account and their states.
     * </p>
     * <p>
     * Availability zones are not the same across accounts. The availability
     * zone <code>us-east-1a</code> for account A is not necessarily the same
     * as <code>us-east-1a</code> for account B. Zone assignments are mapped
     * independently for each account.
     * </p>
     *
     * @param describeAvailabilityZonesRequest Container for the necessary
     *           parameters to execute the DescribeAvailabilityZones operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeAvailabilityZones service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeAvailabilityZonesResult> describeAvailabilityZonesAsync(final DescribeAvailabilityZonesRequest describeAvailabilityZonesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeAvailabilityZonesResult>() {
            public DescribeAvailabilityZonesResult call() throws Exception {
                return describeAvailabilityZones(describeAvailabilityZonesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeAvailabilityZones operation describes availability zones
     * that are currently available to the account and their states.
     * </p>
     * <p>
     * Availability zones are not the same across accounts. The availability
     * zone <code>us-east-1a</code> for account A is not necessarily the same
     * as <code>us-east-1a</code> for account B. Zone assignments are mapped
     * independently for each account.
     * </p>
     *
     * @param describeAvailabilityZonesRequest Container for the necessary
     *           parameters to execute the DescribeAvailabilityZones operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeAvailabilityZones service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeAvailabilityZonesResult> describeAvailabilityZonesAsync(
            final DescribeAvailabilityZonesRequest describeAvailabilityZonesRequest,
            final AsyncHandler<DescribeAvailabilityZonesRequest, DescribeAvailabilityZonesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeAvailabilityZonesResult>() {
            public DescribeAvailabilityZonesResult call() throws Exception {
            	DescribeAvailabilityZonesResult result;
                try {
            		result = describeAvailabilityZones(describeAvailabilityZonesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeAvailabilityZonesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Detach a previously attached volume from a running instance.
     * </p>
     *
     * @param detachVolumeRequest Container for the necessary parameters to
     *           execute the DetachVolume operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DetachVolume service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DetachVolumeResult> detachVolumeAsync(final DetachVolumeRequest detachVolumeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DetachVolumeResult>() {
            public DetachVolumeResult call() throws Exception {
                return detachVolume(detachVolumeRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Detach a previously attached volume from a running instance.
     * </p>
     *
     * @param detachVolumeRequest Container for the necessary parameters to
     *           execute the DetachVolume operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DetachVolume service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DetachVolumeResult> detachVolumeAsync(
            final DetachVolumeRequest detachVolumeRequest,
            final AsyncHandler<DetachVolumeRequest, DetachVolumeResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DetachVolumeResult>() {
            public DetachVolumeResult call() throws Exception {
            	DetachVolumeResult result;
                try {
            		result = detachVolume(detachVolumeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(detachVolumeRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DeleteKeyPair operation deletes a key pair.
     * </p>
     *
     * @param deleteKeyPairRequest Container for the necessary parameters to
     *           execute the DeleteKeyPair operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteKeyPair service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteKeyPairAsync(final DeleteKeyPairRequest deleteKeyPairRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                deleteKeyPair(deleteKeyPairRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * The DeleteKeyPair operation deletes a key pair.
     * </p>
     *
     * @param deleteKeyPairRequest Container for the necessary parameters to
     *           execute the DeleteKeyPair operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteKeyPair service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteKeyPairAsync(
            final DeleteKeyPairRequest deleteKeyPairRequest,
            final AsyncHandler<DeleteKeyPairRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		deleteKeyPair(deleteKeyPairRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(deleteKeyPairRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * Disables monitoring for a running instance.
     * </p>
     *
     * @param unmonitorInstancesRequest Container for the necessary
     *           parameters to execute the UnmonitorInstances operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         UnmonitorInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<UnmonitorInstancesResult> unmonitorInstancesAsync(final UnmonitorInstancesRequest unmonitorInstancesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<UnmonitorInstancesResult>() {
            public UnmonitorInstancesResult call() throws Exception {
                return unmonitorInstances(unmonitorInstancesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Disables monitoring for a running instance.
     * </p>
     *
     * @param unmonitorInstancesRequest Container for the necessary
     *           parameters to execute the UnmonitorInstances operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         UnmonitorInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<UnmonitorInstancesResult> unmonitorInstancesAsync(
            final UnmonitorInstancesRequest unmonitorInstancesRequest,
            final AsyncHandler<UnmonitorInstancesRequest, UnmonitorInstancesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<UnmonitorInstancesResult>() {
            public UnmonitorInstancesResult call() throws Exception {
            	UnmonitorInstancesResult result;
                try {
            		result = unmonitorInstances(unmonitorInstancesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(unmonitorInstancesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Creates an Amazon EBS-backed AMI from a "running" or "stopped"
     * instance. AMIs that use an Amazon EBS root device boot faster than
     * AMIs that use instance stores. They can be up to 1 TiB in size, use
     * storage that persists on instance failure, and can be stopped and
     * started.
     * </p>
     *
     * @param createImageRequest Container for the necessary parameters to
     *           execute the CreateImage operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CreateImage service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateImageResult> createImageAsync(final CreateImageRequest createImageRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateImageResult>() {
            public CreateImageResult call() throws Exception {
                return createImage(createImageRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Creates an Amazon EBS-backed AMI from a "running" or "stopped"
     * instance. AMIs that use an Amazon EBS root device boot faster than
     * AMIs that use instance stores. They can be up to 1 TiB in size, use
     * storage that persists on instance failure, and can be stopped and
     * started.
     * </p>
     *
     * @param createImageRequest Container for the necessary parameters to
     *           execute the CreateImage operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CreateImage service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateImageResult> createImageAsync(
            final CreateImageRequest createImageRequest,
            final AsyncHandler<CreateImageRequest, CreateImageResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateImageResult>() {
            public CreateImageResult call() throws Exception {
            	CreateImageResult result;
                try {
            		result = createImage(createImageRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(createImageRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DeleteSecurityGroup operation deletes a security group.
     * </p>
     * <p>
     * <b>NOTE:</b> If you attempt to delete a security group that contains
     * instances, a fault is returned. If you attempt to delete a security
     * group that is referenced by another security group, a fault is
     * returned. For example, if security group B has a rule that allows
     * access from security group A, security group A cannot be deleted until
     * the allow rule is removed.
     * </p>
     *
     * @param deleteSecurityGroupRequest Container for the necessary
     *           parameters to execute the DeleteSecurityGroup operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteSecurityGroup service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteSecurityGroupAsync(final DeleteSecurityGroupRequest deleteSecurityGroupRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                deleteSecurityGroup(deleteSecurityGroupRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * The DeleteSecurityGroup operation deletes a security group.
     * </p>
     * <p>
     * <b>NOTE:</b> If you attempt to delete a security group that contains
     * instances, a fault is returned. If you attempt to delete a security
     * group that is referenced by another security group, a fault is
     * returned. For example, if security group B has a rule that allows
     * access from security group A, security group A cannot be deleted until
     * the allow rule is removed.
     * </p>
     *
     * @param deleteSecurityGroupRequest Container for the necessary
     *           parameters to execute the DeleteSecurityGroup operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteSecurityGroup service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteSecurityGroupAsync(
            final DeleteSecurityGroupRequest deleteSecurityGroupRequest,
            final AsyncHandler<DeleteSecurityGroupRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		deleteSecurityGroup(deleteSecurityGroupRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(deleteSecurityGroupRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     *
     * @param createInstanceExportTaskRequest Container for the necessary
     *           parameters to execute the CreateInstanceExportTask operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CreateInstanceExportTask service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateInstanceExportTaskResult> createInstanceExportTaskAsync(final CreateInstanceExportTaskRequest createInstanceExportTaskRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateInstanceExportTaskResult>() {
            public CreateInstanceExportTaskResult call() throws Exception {
                return createInstanceExportTask(createInstanceExportTaskRequest);
		    }
		});
    }

    
    /**
     *
     * @param createInstanceExportTaskRequest Container for the necessary
     *           parameters to execute the CreateInstanceExportTask operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CreateInstanceExportTask service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateInstanceExportTaskResult> createInstanceExportTaskAsync(
            final CreateInstanceExportTaskRequest createInstanceExportTaskRequest,
            final AsyncHandler<CreateInstanceExportTaskRequest, CreateInstanceExportTaskResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateInstanceExportTaskResult>() {
            public CreateInstanceExportTaskResult call() throws Exception {
            	CreateInstanceExportTaskResult result;
                try {
            		result = createInstanceExportTask(createInstanceExportTaskRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(createInstanceExportTaskRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Retrieves the encrypted administrator password for the instances
     * running Windows.
     * </p>
     * <p>
     * <b>NOTE:</b> The Windows password is only generated the first time an
     * AMI is launched. It is not generated for rebundled AMIs or after the
     * password is changed on an instance. The password is encrypted using
     * the key pair that you provided.
     * </p>
     *
     * @param getPasswordDataRequest Container for the necessary parameters
     *           to execute the GetPasswordData operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         GetPasswordData service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<GetPasswordDataResult> getPasswordDataAsync(final GetPasswordDataRequest getPasswordDataRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<GetPasswordDataResult>() {
            public GetPasswordDataResult call() throws Exception {
                return getPasswordData(getPasswordDataRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Retrieves the encrypted administrator password for the instances
     * running Windows.
     * </p>
     * <p>
     * <b>NOTE:</b> The Windows password is only generated the first time an
     * AMI is launched. It is not generated for rebundled AMIs or after the
     * password is changed on an instance. The password is encrypted using
     * the key pair that you provided.
     * </p>
     *
     * @param getPasswordDataRequest Container for the necessary parameters
     *           to execute the GetPasswordData operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         GetPasswordData service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<GetPasswordDataResult> getPasswordDataAsync(
            final GetPasswordDataRequest getPasswordDataRequest,
            final AsyncHandler<GetPasswordDataRequest, GetPasswordDataResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<GetPasswordDataResult>() {
            public GetPasswordDataResult call() throws Exception {
            	GetPasswordDataResult result;
                try {
            		result = getPasswordData(getPasswordDataRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(getPasswordDataRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Stops an instance that uses an Amazon EBS volume as its root device.
     * Instances that use Amazon EBS volumes as their root devices can be
     * quickly stopped and started. When an instance is stopped, the compute
     * resources are released and you are not billed for hourly instance
     * usage. However, your root partition Amazon EBS volume remains,
     * continues to persist your data, and you are charged for Amazon EBS
     * volume usage. You can restart your instance at any time.
     * </p>
     * <p>
     * <b>NOTE:</b> Before stopping an instance, make sure it is in a state
     * from which it can be restarted. Stopping an instance does not preserve
     * data stored in RAM. Performing this operation on an instance that uses
     * an instance store as its root device returns an error.
     * </p>
     *
     * @param stopInstancesRequest Container for the necessary parameters to
     *           execute the StopInstances operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         StopInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<StopInstancesResult> stopInstancesAsync(final StopInstancesRequest stopInstancesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<StopInstancesResult>() {
            public StopInstancesResult call() throws Exception {
                return stopInstances(stopInstancesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Stops an instance that uses an Amazon EBS volume as its root device.
     * Instances that use Amazon EBS volumes as their root devices can be
     * quickly stopped and started. When an instance is stopped, the compute
     * resources are released and you are not billed for hourly instance
     * usage. However, your root partition Amazon EBS volume remains,
     * continues to persist your data, and you are charged for Amazon EBS
     * volume usage. You can restart your instance at any time.
     * </p>
     * <p>
     * <b>NOTE:</b> Before stopping an instance, make sure it is in a state
     * from which it can be restarted. Stopping an instance does not preserve
     * data stored in RAM. Performing this operation on an instance that uses
     * an instance store as its root device returns an error.
     * </p>
     *
     * @param stopInstancesRequest Container for the necessary parameters to
     *           execute the StopInstances operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         StopInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<StopInstancesResult> stopInstancesAsync(
            final StopInstancesRequest stopInstancesRequest,
            final AsyncHandler<StopInstancesRequest, StopInstancesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<StopInstancesResult>() {
            public StopInstancesResult call() throws Exception {
            	StopInstancesResult result;
                try {
            		result = stopInstances(stopInstancesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(stopInstancesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Imports the public key from an RSA key pair created with a
     * third-party tool. This operation differs from CreateKeyPair as the
     * private key is never transferred between the caller and AWS servers.
     * </p>
     * <p>
     * RSA key pairs are easily created on Microsoft Windows and Linux OS
     * systems using the <code>ssh-keygen</code> command line tool provided
     * with the standard OpenSSH installation. Standard library support for
     * RSA key pair creation is also available for Java, Ruby, Python, and
     * many other programming languages.
     * </p>
     * <p>
     * The following formats are supported:
     * </p>
     * 
     * <ul>
     * <li> OpenSSH public key format, </li>
     * <li> Base64 encoded DER format. </li>
     * <li> SSH public key file format as specified in <a
     * href="http://tools.ietf.org/html/rfc4716"> RFC4716 </a> . </li>
     * 
     * </ul>
     *
     * @param importKeyPairRequest Container for the necessary parameters to
     *           execute the ImportKeyPair operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ImportKeyPair service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<ImportKeyPairResult> importKeyPairAsync(final ImportKeyPairRequest importKeyPairRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<ImportKeyPairResult>() {
            public ImportKeyPairResult call() throws Exception {
                return importKeyPair(importKeyPairRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Imports the public key from an RSA key pair created with a
     * third-party tool. This operation differs from CreateKeyPair as the
     * private key is never transferred between the caller and AWS servers.
     * </p>
     * <p>
     * RSA key pairs are easily created on Microsoft Windows and Linux OS
     * systems using the <code>ssh-keygen</code> command line tool provided
     * with the standard OpenSSH installation. Standard library support for
     * RSA key pair creation is also available for Java, Ruby, Python, and
     * many other programming languages.
     * </p>
     * <p>
     * The following formats are supported:
     * </p>
     * 
     * <ul>
     * <li> OpenSSH public key format, </li>
     * <li> Base64 encoded DER format. </li>
     * <li> SSH public key file format as specified in <a
     * href="http://tools.ietf.org/html/rfc4716"> RFC4716 </a> . </li>
     * 
     * </ul>
     *
     * @param importKeyPairRequest Container for the necessary parameters to
     *           execute the ImportKeyPair operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ImportKeyPair service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<ImportKeyPairResult> importKeyPairAsync(
            final ImportKeyPairRequest importKeyPairRequest,
            final AsyncHandler<ImportKeyPairRequest, ImportKeyPairResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<ImportKeyPairResult>() {
            public ImportKeyPairResult call() throws Exception {
            	ImportKeyPairResult result;
                try {
            		result = importKeyPair(importKeyPairRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(importKeyPairRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The CreateSecurityGroup operation creates a new security group.
     * </p>
     * <p>
     * Every instance is launched in a security group. If no security group
     * is specified during launch, the instances are launched in the default
     * security group. Instances within the same security group have
     * unrestricted network access to each other. Instances will reject
     * network access attempts from other instances in a different security
     * group. As the owner of instances you can grant or revoke specific
     * permissions using the AuthorizeSecurityGroupIngress and
     * RevokeSecurityGroupIngress operations.
     * </p>
     *
     * @param createSecurityGroupRequest Container for the necessary
     *           parameters to execute the CreateSecurityGroup operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CreateSecurityGroup service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateSecurityGroupResult> createSecurityGroupAsync(final CreateSecurityGroupRequest createSecurityGroupRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateSecurityGroupResult>() {
            public CreateSecurityGroupResult call() throws Exception {
                return createSecurityGroup(createSecurityGroupRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The CreateSecurityGroup operation creates a new security group.
     * </p>
     * <p>
     * Every instance is launched in a security group. If no security group
     * is specified during launch, the instances are launched in the default
     * security group. Instances within the same security group have
     * unrestricted network access to each other. Instances will reject
     * network access attempts from other instances in a different security
     * group. As the owner of instances you can grant or revoke specific
     * permissions using the AuthorizeSecurityGroupIngress and
     * RevokeSecurityGroupIngress operations.
     * </p>
     *
     * @param createSecurityGroupRequest Container for the necessary
     *           parameters to execute the CreateSecurityGroup operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CreateSecurityGroup service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateSecurityGroupResult> createSecurityGroupAsync(
            final CreateSecurityGroupRequest createSecurityGroupRequest,
            final AsyncHandler<CreateSecurityGroupRequest, CreateSecurityGroupResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateSecurityGroupResult>() {
            public CreateSecurityGroupResult call() throws Exception {
            	CreateSecurityGroupResult result;
                try {
            		result = createSecurityGroup(createSecurityGroupRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(createSecurityGroupRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Describes the Spot Price history.
     * </p>
     * <p>
     * Spot Instances are instances that Amazon EC2 starts on your behalf
     * when the maximum price that you specify exceeds the current Spot
     * Price. Amazon EC2 periodically sets the Spot Price based on available
     * Spot Instance capacity and current spot instance requests.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide .
     * 
     * </p>
     *
     * @param describeSpotPriceHistoryRequest Container for the necessary
     *           parameters to execute the DescribeSpotPriceHistory operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSpotPriceHistory service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSpotPriceHistoryResult> describeSpotPriceHistoryAsync(final DescribeSpotPriceHistoryRequest describeSpotPriceHistoryRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSpotPriceHistoryResult>() {
            public DescribeSpotPriceHistoryResult call() throws Exception {
                return describeSpotPriceHistory(describeSpotPriceHistoryRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Describes the Spot Price history.
     * </p>
     * <p>
     * Spot Instances are instances that Amazon EC2 starts on your behalf
     * when the maximum price that you specify exceeds the current Spot
     * Price. Amazon EC2 periodically sets the Spot Price based on available
     * Spot Instance capacity and current spot instance requests.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide .
     * 
     * </p>
     *
     * @param describeSpotPriceHistoryRequest Container for the necessary
     *           parameters to execute the DescribeSpotPriceHistory operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSpotPriceHistory service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSpotPriceHistoryResult> describeSpotPriceHistoryAsync(
            final DescribeSpotPriceHistoryRequest describeSpotPriceHistoryRequest,
            final AsyncHandler<DescribeSpotPriceHistoryRequest, DescribeSpotPriceHistoryResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSpotPriceHistoryResult>() {
            public DescribeSpotPriceHistoryResult call() throws Exception {
            	DescribeSpotPriceHistoryResult result;
                try {
            		result = describeSpotPriceHistory(describeSpotPriceHistoryRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeSpotPriceHistoryRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeRegions operation describes regions zones that are
     * currently available to the account.
     * </p>
     *
     * @param describeRegionsRequest Container for the necessary parameters
     *           to execute the DescribeRegions operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeRegions service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeRegionsResult> describeRegionsAsync(final DescribeRegionsRequest describeRegionsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeRegionsResult>() {
            public DescribeRegionsResult call() throws Exception {
                return describeRegions(describeRegionsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeRegions operation describes regions zones that are
     * currently available to the account.
     * </p>
     *
     * @param describeRegionsRequest Container for the necessary parameters
     *           to execute the DescribeRegions operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeRegions service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeRegionsResult> describeRegionsAsync(
            final DescribeRegionsRequest describeRegionsRequest,
            final AsyncHandler<DescribeRegionsRequest, DescribeRegionsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeRegionsResult>() {
            public DescribeRegionsResult call() throws Exception {
            	DescribeRegionsResult result;
                try {
            		result = describeRegions(describeRegionsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeRegionsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     *
     * @param createReservedInstancesListingRequest Container for the
     *           necessary parameters to execute the CreateReservedInstancesListing
     *           operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CreateReservedInstancesListing service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateReservedInstancesListingResult> createReservedInstancesListingAsync(final CreateReservedInstancesListingRequest createReservedInstancesListingRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateReservedInstancesListingResult>() {
            public CreateReservedInstancesListingResult call() throws Exception {
                return createReservedInstancesListing(createReservedInstancesListingRequest);
		    }
		});
    }

    
    /**
     *
     * @param createReservedInstancesListingRequest Container for the
     *           necessary parameters to execute the CreateReservedInstancesListing
     *           operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CreateReservedInstancesListing service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateReservedInstancesListingResult> createReservedInstancesListingAsync(
            final CreateReservedInstancesListingRequest createReservedInstancesListingRequest,
            final AsyncHandler<CreateReservedInstancesListingRequest, CreateReservedInstancesListingResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateReservedInstancesListingResult>() {
            public CreateReservedInstancesListingResult call() throws Exception {
            	CreateReservedInstancesListingResult result;
                try {
            		result = createReservedInstancesListing(createReservedInstancesListingRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(createReservedInstancesListingRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Resets permission settings for the specified snapshot.
     * </p>
     *
     * @param resetSnapshotAttributeRequest Container for the necessary
     *           parameters to execute the ResetSnapshotAttribute operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ResetSnapshotAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> resetSnapshotAttributeAsync(final ResetSnapshotAttributeRequest resetSnapshotAttributeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                resetSnapshotAttribute(resetSnapshotAttributeRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Resets permission settings for the specified snapshot.
     * </p>
     *
     * @param resetSnapshotAttributeRequest Container for the necessary
     *           parameters to execute the ResetSnapshotAttribute operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ResetSnapshotAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> resetSnapshotAttributeAsync(
            final ResetSnapshotAttributeRequest resetSnapshotAttributeRequest,
            final AsyncHandler<ResetSnapshotAttributeRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		resetSnapshotAttribute(resetSnapshotAttributeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(resetSnapshotAttributeRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     *
     * @param importVolumeRequest Container for the necessary parameters to
     *           execute the ImportVolume operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ImportVolume service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<ImportVolumeResult> importVolumeAsync(final ImportVolumeRequest importVolumeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<ImportVolumeResult>() {
            public ImportVolumeResult call() throws Exception {
                return importVolume(importVolumeRequest);
		    }
		});
    }

    
    /**
     *
     * @param importVolumeRequest Container for the necessary parameters to
     *           execute the ImportVolume operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ImportVolume service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<ImportVolumeResult> importVolumeAsync(
            final ImportVolumeRequest importVolumeRequest,
            final AsyncHandler<ImportVolumeRequest, ImportVolumeResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<ImportVolumeResult>() {
            public ImportVolumeResult call() throws Exception {
            	ImportVolumeResult result;
                try {
            		result = importVolume(importVolumeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(importVolumeRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeSecurityGroups operation returns information about
     * security groups that you own.
     * </p>
     * <p>
     * If you specify security group names, information about those security
     * group is returned. Otherwise, information for all security group is
     * returned. If you specify a group that does not exist, a fault is
     * returned.
     * </p>
     *
     * @param describeSecurityGroupsRequest Container for the necessary
     *           parameters to execute the DescribeSecurityGroups operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSecurityGroups service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSecurityGroupsResult> describeSecurityGroupsAsync(final DescribeSecurityGroupsRequest describeSecurityGroupsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSecurityGroupsResult>() {
            public DescribeSecurityGroupsResult call() throws Exception {
                return describeSecurityGroups(describeSecurityGroupsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeSecurityGroups operation returns information about
     * security groups that you own.
     * </p>
     * <p>
     * If you specify security group names, information about those security
     * group is returned. Otherwise, information for all security group is
     * returned. If you specify a group that does not exist, a fault is
     * returned.
     * </p>
     *
     * @param describeSecurityGroupsRequest Container for the necessary
     *           parameters to execute the DescribeSecurityGroups operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSecurityGroups service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSecurityGroupsResult> describeSecurityGroupsAsync(
            final DescribeSecurityGroupsRequest describeSecurityGroupsRequest,
            final AsyncHandler<DescribeSecurityGroupsRequest, DescribeSecurityGroupsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSecurityGroupsResult>() {
            public DescribeSecurityGroupsResult call() throws Exception {
            	DescribeSecurityGroupsResult result;
                try {
            		result = describeSecurityGroups(describeSecurityGroupsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeSecurityGroupsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DeregisterImage operation deregisters an AMI. Once deregistered,
     * instances of the AMI can no longer be launched.
     * </p>
     *
     * @param deregisterImageRequest Container for the necessary parameters
     *           to execute the DeregisterImage operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DeregisterImage service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deregisterImageAsync(final DeregisterImageRequest deregisterImageRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                deregisterImage(deregisterImageRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * The DeregisterImage operation deregisters an AMI. Once deregistered,
     * instances of the AMI can no longer be launched.
     * </p>
     *
     * @param deregisterImageRequest Container for the necessary parameters
     *           to execute the DeregisterImage operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DeregisterImage service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deregisterImageAsync(
            final DeregisterImageRequest deregisterImageRequest,
            final AsyncHandler<DeregisterImageRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		deregisterImage(deregisterImageRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(deregisterImageRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * Describes the data feed for Spot Instances.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide .
     * 
     * </p>
     *
     * @param describeSpotDatafeedSubscriptionRequest Container for the
     *           necessary parameters to execute the DescribeSpotDatafeedSubscription
     *           operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSpotDatafeedSubscription service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSpotDatafeedSubscriptionResult> describeSpotDatafeedSubscriptionAsync(final DescribeSpotDatafeedSubscriptionRequest describeSpotDatafeedSubscriptionRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSpotDatafeedSubscriptionResult>() {
            public DescribeSpotDatafeedSubscriptionResult call() throws Exception {
                return describeSpotDatafeedSubscription(describeSpotDatafeedSubscriptionRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Describes the data feed for Spot Instances.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide .
     * 
     * </p>
     *
     * @param describeSpotDatafeedSubscriptionRequest Container for the
     *           necessary parameters to execute the DescribeSpotDatafeedSubscription
     *           operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSpotDatafeedSubscription service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSpotDatafeedSubscriptionResult> describeSpotDatafeedSubscriptionAsync(
            final DescribeSpotDatafeedSubscriptionRequest describeSpotDatafeedSubscriptionRequest,
            final AsyncHandler<DescribeSpotDatafeedSubscriptionRequest, DescribeSpotDatafeedSubscriptionResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSpotDatafeedSubscriptionResult>() {
            public DescribeSpotDatafeedSubscriptionResult call() throws Exception {
            	DescribeSpotDatafeedSubscriptionResult result;
                try {
            		result = describeSpotDatafeedSubscription(describeSpotDatafeedSubscriptionRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeSpotDatafeedSubscriptionRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Deletes tags from the specified Amazon EC2 resources.
     * </p>
     *
     * @param deleteTagsRequest Container for the necessary parameters to
     *           execute the DeleteTags operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteTags service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteTagsAsync(final DeleteTagsRequest deleteTagsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                deleteTags(deleteTagsRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Deletes tags from the specified Amazon EC2 resources.
     * </p>
     *
     * @param deleteTagsRequest Container for the necessary parameters to
     *           execute the DeleteTags operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteTags service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteTagsAsync(
            final DeleteTagsRequest deleteTagsRequest,
            final AsyncHandler<DeleteTagsRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		deleteTags(deleteTagsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(deleteTagsRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     *
     * @param describeAccountAttributesRequest Container for the necessary
     *           parameters to execute the DescribeAccountAttributes operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeAccountAttributes service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeAccountAttributesResult> describeAccountAttributesAsync(final DescribeAccountAttributesRequest describeAccountAttributesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeAccountAttributesResult>() {
            public DescribeAccountAttributesResult call() throws Exception {
                return describeAccountAttributes(describeAccountAttributesRequest);
		    }
		});
    }

    
    /**
     *
     * @param describeAccountAttributesRequest Container for the necessary
     *           parameters to execute the DescribeAccountAttributes operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeAccountAttributes service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeAccountAttributesResult> describeAccountAttributesAsync(
            final DescribeAccountAttributesRequest describeAccountAttributesRequest,
            final AsyncHandler<DescribeAccountAttributesRequest, DescribeAccountAttributesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeAccountAttributesResult>() {
            public DescribeAccountAttributesResult call() throws Exception {
            	DescribeAccountAttributesResult result;
                try {
            		result = describeAccountAttributes(describeAccountAttributesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeAccountAttributesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Enable IO on the volume after an event has occured.
     * </p>
     *
     * @param enableVolumeIORequest Container for the necessary parameters to
     *           execute the EnableVolumeIO operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         EnableVolumeIO service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> enableVolumeIOAsync(final EnableVolumeIORequest enableVolumeIORequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                enableVolumeIO(enableVolumeIORequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Enable IO on the volume after an event has occured.
     * </p>
     *
     * @param enableVolumeIORequest Container for the necessary parameters to
     *           execute the EnableVolumeIO operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         EnableVolumeIO service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> enableVolumeIOAsync(
            final EnableVolumeIORequest enableVolumeIORequest,
            final AsyncHandler<EnableVolumeIORequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		enableVolumeIO(enableVolumeIORequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(enableVolumeIORequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * Attach a previously created volume to a running instance.
     * </p>
     *
     * @param attachVolumeRequest Container for the necessary parameters to
     *           execute the AttachVolume operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         AttachVolume service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<AttachVolumeResult> attachVolumeAsync(final AttachVolumeRequest attachVolumeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<AttachVolumeResult>() {
            public AttachVolumeResult call() throws Exception {
                return attachVolume(attachVolumeRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Attach a previously created volume to a running instance.
     * </p>
     *
     * @param attachVolumeRequest Container for the necessary parameters to
     *           execute the AttachVolume operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         AttachVolume service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<AttachVolumeResult> attachVolumeAsync(
            final AttachVolumeRequest attachVolumeRequest,
            final AsyncHandler<AttachVolumeRequest, AttachVolumeResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<AttachVolumeResult>() {
            public AttachVolumeResult call() throws Exception {
            	AttachVolumeResult result;
                try {
            		result = attachVolume(attachVolumeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(attachVolumeRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Provides details of a user's registered licenses. Zero or more IDs
     * may be specified on the call. When one or more license IDs are
     * specified, only data for the specified IDs are returned.
     * </p>
     *
     * @param describeLicensesRequest Container for the necessary parameters
     *           to execute the DescribeLicenses operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeLicenses service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeLicensesResult> describeLicensesAsync(final DescribeLicensesRequest describeLicensesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeLicensesResult>() {
            public DescribeLicensesResult call() throws Exception {
                return describeLicenses(describeLicensesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Provides details of a user's registered licenses. Zero or more IDs
     * may be specified on the call. When one or more license IDs are
     * specified, only data for the specified IDs are returned.
     * </p>
     *
     * @param describeLicensesRequest Container for the necessary parameters
     *           to execute the DescribeLicenses operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeLicenses service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeLicensesResult> describeLicensesAsync(
            final DescribeLicensesRequest describeLicensesRequest,
            final AsyncHandler<DescribeLicensesRequest, DescribeLicensesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeLicensesResult>() {
            public DescribeLicensesResult call() throws Exception {
            	DescribeLicensesResult result;
                try {
            		result = describeLicenses(describeLicensesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeLicensesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Describes the status of a volume.
     * </p>
     *
     * @param describeVolumeStatusRequest Container for the necessary
     *           parameters to execute the DescribeVolumeStatus operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeVolumeStatus service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeVolumeStatusResult> describeVolumeStatusAsync(final DescribeVolumeStatusRequest describeVolumeStatusRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeVolumeStatusResult>() {
            public DescribeVolumeStatusResult call() throws Exception {
                return describeVolumeStatus(describeVolumeStatusRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Describes the status of a volume.
     * </p>
     *
     * @param describeVolumeStatusRequest Container for the necessary
     *           parameters to execute the DescribeVolumeStatus operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeVolumeStatus service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeVolumeStatusResult> describeVolumeStatusAsync(
            final DescribeVolumeStatusRequest describeVolumeStatusRequest,
            final AsyncHandler<DescribeVolumeStatusRequest, DescribeVolumeStatusResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeVolumeStatusResult>() {
            public DescribeVolumeStatusResult call() throws Exception {
            	DescribeVolumeStatusResult result;
                try {
            		result = describeVolumeStatus(describeVolumeStatusRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeVolumeStatusRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Activates a specific number of licenses for a 90-day period.
     * Activations can be done against a specific license ID.
     * </p>
     *
     * @param activateLicenseRequest Container for the necessary parameters
     *           to execute the ActivateLicense operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ActivateLicense service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> activateLicenseAsync(final ActivateLicenseRequest activateLicenseRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                activateLicense(activateLicenseRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Activates a specific number of licenses for a 90-day period.
     * Activations can be done against a specific license ID.
     * </p>
     *
     * @param activateLicenseRequest Container for the necessary parameters
     *           to execute the ActivateLicense operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ActivateLicense service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> activateLicenseAsync(
            final ActivateLicenseRequest activateLicenseRequest,
            final AsyncHandler<ActivateLicenseRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		activateLicense(activateLicenseRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(activateLicenseRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * The ResetImageAttribute operation resets an attribute of an AMI to
     * its default value.
     * </p>
     * <p>
     * <b>NOTE:</b> The productCodes attribute cannot be reset.
     * </p>
     *
     * @param resetImageAttributeRequest Container for the necessary
     *           parameters to execute the ResetImageAttribute operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ResetImageAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> resetImageAttributeAsync(final ResetImageAttributeRequest resetImageAttributeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                resetImageAttribute(resetImageAttributeRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * The ResetImageAttribute operation resets an attribute of an AMI to
     * its default value.
     * </p>
     * <p>
     * <b>NOTE:</b> The productCodes attribute cannot be reset.
     * </p>
     *
     * @param resetImageAttributeRequest Container for the necessary
     *           parameters to execute the ResetImageAttribute operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ResetImageAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> resetImageAttributeAsync(
            final ResetImageAttributeRequest resetImageAttributeRequest,
            final AsyncHandler<ResetImageAttributeRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		resetImageAttribute(resetImageAttributeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(resetImageAttributeRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * Create a snapshot of the volume identified by volume ID. A volume
     * does not have to be detached at the time the snapshot is taken.
     * </p>
     * <p>
     * <b>NOTE:</b> Snapshot creation requires that the system is in a
     * consistent state. For instance, this means that if taking a snapshot
     * of a database, the tables must be read-only locked to ensure that the
     * snapshot will not contain a corrupted version of the database.
     * Therefore, be careful when using this API to ensure that the system
     * remains in the consistent state until the create snapshot status has
     * returned.
     * </p>
     *
     * @param createSnapshotRequest Container for the necessary parameters to
     *           execute the CreateSnapshot operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CreateSnapshot service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateSnapshotResult> createSnapshotAsync(final CreateSnapshotRequest createSnapshotRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateSnapshotResult>() {
            public CreateSnapshotResult call() throws Exception {
                return createSnapshot(createSnapshotRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Create a snapshot of the volume identified by volume ID. A volume
     * does not have to be detached at the time the snapshot is taken.
     * </p>
     * <p>
     * <b>NOTE:</b> Snapshot creation requires that the system is in a
     * consistent state. For instance, this means that if taking a snapshot
     * of a database, the tables must be read-only locked to ensure that the
     * snapshot will not contain a corrupted version of the database.
     * Therefore, be careful when using this API to ensure that the system
     * remains in the consistent state until the create snapshot status has
     * returned.
     * </p>
     *
     * @param createSnapshotRequest Container for the necessary parameters to
     *           execute the CreateSnapshot operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CreateSnapshot service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateSnapshotResult> createSnapshotAsync(
            final CreateSnapshotRequest createSnapshotRequest,
            final AsyncHandler<CreateSnapshotRequest, CreateSnapshotResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateSnapshotResult>() {
            public CreateSnapshotResult call() throws Exception {
            	CreateSnapshotResult result;
                try {
            		result = createSnapshot(createSnapshotRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(createSnapshotRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Deletes a previously created volume. Once successfully deleted, a
     * new volume can be created with the same name.
     * </p>
     *
     * @param deleteVolumeRequest Container for the necessary parameters to
     *           execute the DeleteVolume operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteVolume service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteVolumeAsync(final DeleteVolumeRequest deleteVolumeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                deleteVolume(deleteVolumeRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Deletes a previously created volume. Once successfully deleted, a
     * new volume can be created with the same name.
     * </p>
     *
     * @param deleteVolumeRequest Container for the necessary parameters to
     *           execute the DeleteVolume operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteVolume service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteVolumeAsync(
            final DeleteVolumeRequest deleteVolumeRequest,
            final AsyncHandler<DeleteVolumeRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		deleteVolume(deleteVolumeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(deleteVolumeRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * The ModifyReservedInstances operation modifies the Availability Zone,
     * instance count, instance type, or network platform (EC2-Classic or
     * EC2-VPC) of your Reserved Instances.
     * </p>
     *
     * @param modifyReservedInstancesRequest Container for the necessary
     *           parameters to execute the ModifyReservedInstances operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ModifyReservedInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<ModifyReservedInstancesResult> modifyReservedInstancesAsync(final ModifyReservedInstancesRequest modifyReservedInstancesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<ModifyReservedInstancesResult>() {
            public ModifyReservedInstancesResult call() throws Exception {
                return modifyReservedInstances(modifyReservedInstancesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The ModifyReservedInstances operation modifies the Availability Zone,
     * instance count, instance type, or network platform (EC2-Classic or
     * EC2-VPC) of your Reserved Instances.
     * </p>
     *
     * @param modifyReservedInstancesRequest Container for the necessary
     *           parameters to execute the ModifyReservedInstances operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ModifyReservedInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<ModifyReservedInstancesResult> modifyReservedInstancesAsync(
            final ModifyReservedInstancesRequest modifyReservedInstancesRequest,
            final AsyncHandler<ModifyReservedInstancesRequest, ModifyReservedInstancesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<ModifyReservedInstancesResult>() {
            public ModifyReservedInstancesResult call() throws Exception {
            	ModifyReservedInstancesResult result;
                try {
            		result = modifyReservedInstances(modifyReservedInstancesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(modifyReservedInstancesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Gives you information about your VPCs. You can filter the results to
     * return information only about VPCs that match criteria you specify.
     * </p>
     * <p>
     * For example, you could ask to get information about a particular VPC
     * or VPCs (or all your VPCs) only if the VPC's state is available. You
     * can specify multiple filters (e.g., the VPC uses one of several sets
     * of DHCP options, and the VPC's state is available). The result
     * includes information for a particular VPC only if the VPC matches all
     * your filters.
     * </p>
     * <p>
     * If there's no match, no special message is returned; the response is
     * simply empty. The following table shows the available filters.
     * </p>
     *
     * @param describeVpcsRequest Container for the necessary parameters to
     *           execute the DescribeVpcs operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeVpcs service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeVpcsResult> describeVpcsAsync(final DescribeVpcsRequest describeVpcsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeVpcsResult>() {
            public DescribeVpcsResult call() throws Exception {
                return describeVpcs(describeVpcsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Gives you information about your VPCs. You can filter the results to
     * return information only about VPCs that match criteria you specify.
     * </p>
     * <p>
     * For example, you could ask to get information about a particular VPC
     * or VPCs (or all your VPCs) only if the VPC's state is available. You
     * can specify multiple filters (e.g., the VPC uses one of several sets
     * of DHCP options, and the VPC's state is available). The result
     * includes information for a particular VPC only if the VPC matches all
     * your filters.
     * </p>
     * <p>
     * If there's no match, no special message is returned; the response is
     * simply empty. The following table shows the available filters.
     * </p>
     *
     * @param describeVpcsRequest Container for the necessary parameters to
     *           execute the DescribeVpcs operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeVpcs service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeVpcsResult> describeVpcsAsync(
            final DescribeVpcsRequest describeVpcsRequest,
            final AsyncHandler<DescribeVpcsRequest, DescribeVpcsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeVpcsResult>() {
            public DescribeVpcsResult call() throws Exception {
            	DescribeVpcsResult result;
                try {
            		result = describeVpcs(describeVpcsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeVpcsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     *
     * @param cancelConversionTaskRequest Container for the necessary
     *           parameters to execute the CancelConversionTask operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CancelConversionTask service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> cancelConversionTaskAsync(final CancelConversionTaskRequest cancelConversionTaskRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                cancelConversionTask(cancelConversionTaskRequest);
                return null;
		    }
		});
    }

    
    /**
     *
     * @param cancelConversionTaskRequest Container for the necessary
     *           parameters to execute the CancelConversionTask operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CancelConversionTask service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> cancelConversionTaskAsync(
            final CancelConversionTaskRequest cancelConversionTaskRequest,
            final AsyncHandler<CancelConversionTaskRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		cancelConversionTask(cancelConversionTaskRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(cancelConversionTaskRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * The AssociateAddress operation associates an elastic IP address with
     * an instance.
     * </p>
     * <p>
     * If the IP address is currently assigned to another instance, the IP
     * address is assigned to the new instance. This is an idempotent
     * operation. If you enter it more than once, Amazon EC2 does not return
     * an error.
     * </p>
     *
     * @param associateAddressRequest Container for the necessary parameters
     *           to execute the AssociateAddress operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         AssociateAddress service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<AssociateAddressResult> associateAddressAsync(final AssociateAddressRequest associateAddressRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<AssociateAddressResult>() {
            public AssociateAddressResult call() throws Exception {
                return associateAddress(associateAddressRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The AssociateAddress operation associates an elastic IP address with
     * an instance.
     * </p>
     * <p>
     * If the IP address is currently assigned to another instance, the IP
     * address is assigned to the new instance. This is an idempotent
     * operation. If you enter it more than once, Amazon EC2 does not return
     * an error.
     * </p>
     *
     * @param associateAddressRequest Container for the necessary parameters
     *           to execute the AssociateAddress operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         AssociateAddress service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<AssociateAddressResult> associateAddressAsync(
            final AssociateAddressRequest associateAddressRequest,
            final AsyncHandler<AssociateAddressRequest, AssociateAddressResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<AssociateAddressResult>() {
            public AssociateAddressResult call() throws Exception {
            	AssociateAddressResult result;
                try {
            		result = associateAddress(associateAddressRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(associateAddressRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Deactivates a specific number of licenses. Deactivations can be done
     * against a specific license ID after they have persisted for at least a
     * 90-day period.
     * </p>
     *
     * @param deactivateLicenseRequest Container for the necessary parameters
     *           to execute the DeactivateLicense operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DeactivateLicense service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deactivateLicenseAsync(final DeactivateLicenseRequest deactivateLicenseRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                deactivateLicense(deactivateLicenseRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Deactivates a specific number of licenses. Deactivations can be done
     * against a specific license ID after they have persisted for at least a
     * 90-day period.
     * </p>
     *
     * @param deactivateLicenseRequest Container for the necessary parameters
     *           to execute the DeactivateLicense operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DeactivateLicense service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deactivateLicenseAsync(
            final DeactivateLicenseRequest deactivateLicenseRequest,
            final AsyncHandler<DeactivateLicenseRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		deactivateLicense(deactivateLicenseRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(deactivateLicenseRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     *
     * @param describeExportTasksRequest Container for the necessary
     *           parameters to execute the DescribeExportTasks operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeExportTasks service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeExportTasksResult> describeExportTasksAsync(final DescribeExportTasksRequest describeExportTasksRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeExportTasksResult>() {
            public DescribeExportTasksResult call() throws Exception {
                return describeExportTasks(describeExportTasksRequest);
		    }
		});
    }

    
    /**
     *
     * @param describeExportTasksRequest Container for the necessary
     *           parameters to execute the DescribeExportTasks operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeExportTasks service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeExportTasksResult> describeExportTasksAsync(
            final DescribeExportTasksRequest describeExportTasksRequest,
            final AsyncHandler<DescribeExportTasksRequest, DescribeExportTasksResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeExportTasksResult>() {
            public DescribeExportTasksResult call() throws Exception {
            	DescribeExportTasksResult result;
                try {
            		result = describeExportTasks(describeExportTasksRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeExportTasksRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Describes the status of the indicated volume or, in lieu of any
     * specified, all volumes belonging to the caller. Volumes that have been
     * deleted are not described.
     * </p>
     *
     * @param describeVolumesRequest Container for the necessary parameters
     *           to execute the DescribeVolumes operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeVolumes service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeVolumesResult> describeVolumesAsync(final DescribeVolumesRequest describeVolumesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeVolumesResult>() {
            public DescribeVolumesResult call() throws Exception {
                return describeVolumes(describeVolumesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Describes the status of the indicated volume or, in lieu of any
     * specified, all volumes belonging to the caller. Volumes that have been
     * deleted are not described.
     * </p>
     *
     * @param describeVolumesRequest Container for the necessary parameters
     *           to execute the DescribeVolumes operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeVolumes service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeVolumesResult> describeVolumesAsync(
            final DescribeVolumesRequest describeVolumesRequest,
            final AsyncHandler<DescribeVolumesRequest, DescribeVolumesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeVolumesResult>() {
            public DescribeVolumesResult call() throws Exception {
            	DescribeVolumesResult result;
                try {
            		result = describeVolumes(describeVolumesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeVolumesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     *
     * @param describeReservedInstancesListingsRequest Container for the
     *           necessary parameters to execute the DescribeReservedInstancesListings
     *           operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeReservedInstancesListings service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeReservedInstancesListingsResult> describeReservedInstancesListingsAsync(final DescribeReservedInstancesListingsRequest describeReservedInstancesListingsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeReservedInstancesListingsResult>() {
            public DescribeReservedInstancesListingsResult call() throws Exception {
                return describeReservedInstancesListings(describeReservedInstancesListingsRequest);
		    }
		});
    }

    
    /**
     *
     * @param describeReservedInstancesListingsRequest Container for the
     *           necessary parameters to execute the DescribeReservedInstancesListings
     *           operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeReservedInstancesListings service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeReservedInstancesListingsResult> describeReservedInstancesListingsAsync(
            final DescribeReservedInstancesListingsRequest describeReservedInstancesListingsRequest,
            final AsyncHandler<DescribeReservedInstancesListingsRequest, DescribeReservedInstancesListingsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeReservedInstancesListingsResult>() {
            public DescribeReservedInstancesListingsResult call() throws Exception {
            	DescribeReservedInstancesListingsResult result;
                try {
            		result = describeReservedInstancesListings(describeReservedInstancesListingsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeReservedInstancesListingsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     *
     * @param reportInstanceStatusRequest Container for the necessary
     *           parameters to execute the ReportInstanceStatus operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ReportInstanceStatus service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> reportInstanceStatusAsync(final ReportInstanceStatusRequest reportInstanceStatusRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                reportInstanceStatus(reportInstanceStatusRequest);
                return null;
		    }
		});
    }

    
    /**
     *
     * @param reportInstanceStatusRequest Container for the necessary
     *           parameters to execute the ReportInstanceStatus operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ReportInstanceStatus service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> reportInstanceStatusAsync(
            final ReportInstanceStatusRequest reportInstanceStatusRequest,
            final AsyncHandler<ReportInstanceStatusRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		reportInstanceStatus(reportInstanceStatusRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(reportInstanceStatusRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * Enables monitoring for a running instance.
     * </p>
     *
     * @param monitorInstancesRequest Container for the necessary parameters
     *           to execute the MonitorInstances operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         MonitorInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<MonitorInstancesResult> monitorInstancesAsync(final MonitorInstancesRequest monitorInstancesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<MonitorInstancesResult>() {
            public MonitorInstancesResult call() throws Exception {
                return monitorInstances(monitorInstancesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Enables monitoring for a running instance.
     * </p>
     *
     * @param monitorInstancesRequest Container for the necessary parameters
     *           to execute the MonitorInstances operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         MonitorInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<MonitorInstancesResult> monitorInstancesAsync(
            final MonitorInstancesRequest monitorInstancesRequest,
            final AsyncHandler<MonitorInstancesRequest, MonitorInstancesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<MonitorInstancesResult>() {
            public MonitorInstancesResult call() throws Exception {
            	MonitorInstancesResult result;
                try {
            		result = monitorInstances(monitorInstancesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(monitorInstancesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeBundleTasks operation describes in-progress and recent
     * bundle tasks. Complete and failed tasks are removed from the list a
     * short time after completion. If no bundle ids are given, all bundle
     * tasks are returned.
     * </p>
     *
     * @param describeBundleTasksRequest Container for the necessary
     *           parameters to execute the DescribeBundleTasks operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeBundleTasks service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeBundleTasksResult> describeBundleTasksAsync(final DescribeBundleTasksRequest describeBundleTasksRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeBundleTasksResult>() {
            public DescribeBundleTasksResult call() throws Exception {
                return describeBundleTasks(describeBundleTasksRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeBundleTasks operation describes in-progress and recent
     * bundle tasks. Complete and failed tasks are removed from the list a
     * short time after completion. If no bundle ids are given, all bundle
     * tasks are returned.
     * </p>
     *
     * @param describeBundleTasksRequest Container for the necessary
     *           parameters to execute the DescribeBundleTasks operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeBundleTasks service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeBundleTasksResult> describeBundleTasksAsync(
            final DescribeBundleTasksRequest describeBundleTasksRequest,
            final AsyncHandler<DescribeBundleTasksRequest, DescribeBundleTasksResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeBundleTasksResult>() {
            public DescribeBundleTasksResult call() throws Exception {
            	DescribeBundleTasksResult result;
                try {
            		result = describeBundleTasks(describeBundleTasksRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeBundleTasksRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     *
     * @param importInstanceRequest Container for the necessary parameters to
     *           execute the ImportInstance operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ImportInstance service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<ImportInstanceResult> importInstanceAsync(final ImportInstanceRequest importInstanceRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<ImportInstanceResult>() {
            public ImportInstanceResult call() throws Exception {
                return importInstance(importInstanceRequest);
		    }
		});
    }

    
    /**
     *
     * @param importInstanceRequest Container for the necessary parameters to
     *           execute the ImportInstance operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ImportInstance service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<ImportInstanceResult> importInstanceAsync(
            final ImportInstanceRequest importInstanceRequest,
            final AsyncHandler<ImportInstanceRequest, ImportInstanceResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<ImportInstanceResult>() {
            public ImportInstanceResult call() throws Exception {
            	ImportInstanceResult result;
                try {
            		result = importInstance(importInstanceRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(importInstanceRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The RevokeSecurityGroupIngress operation revokes permissions from a
     * security group. The permissions used to revoke must be specified using
     * the same values used to grant the permissions.
     * </p>
     * <p>
     * Permissions are specified by IP protocol (TCP, UDP, or ICMP), the
     * source of the request (by IP range or an Amazon EC2 user-group pair),
     * the source and destination port ranges (for TCP and UDP), and the ICMP
     * codes and types (for ICMP).
     * </p>
     * <p>
     * Permission changes are quickly propagated to instances within the
     * security group. However, depending on the number of instances in the
     * group, a small delay might occur.
     * </p>
     *
     * @param revokeSecurityGroupIngressRequest Container for the necessary
     *           parameters to execute the RevokeSecurityGroupIngress operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         RevokeSecurityGroupIngress service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> revokeSecurityGroupIngressAsync(final RevokeSecurityGroupIngressRequest revokeSecurityGroupIngressRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                revokeSecurityGroupIngress(revokeSecurityGroupIngressRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * The RevokeSecurityGroupIngress operation revokes permissions from a
     * security group. The permissions used to revoke must be specified using
     * the same values used to grant the permissions.
     * </p>
     * <p>
     * Permissions are specified by IP protocol (TCP, UDP, or ICMP), the
     * source of the request (by IP range or an Amazon EC2 user-group pair),
     * the source and destination port ranges (for TCP and UDP), and the ICMP
     * codes and types (for ICMP).
     * </p>
     * <p>
     * Permission changes are quickly propagated to instances within the
     * security group. However, depending on the number of instances in the
     * group, a small delay might occur.
     * </p>
     *
     * @param revokeSecurityGroupIngressRequest Container for the necessary
     *           parameters to execute the RevokeSecurityGroupIngress operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         RevokeSecurityGroupIngress service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> revokeSecurityGroupIngressAsync(
            final RevokeSecurityGroupIngressRequest revokeSecurityGroupIngressRequest,
            final AsyncHandler<RevokeSecurityGroupIngressRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		revokeSecurityGroupIngress(revokeSecurityGroupIngressRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(revokeSecurityGroupIngressRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * The GetConsoleOutput operation retrieves console output for the
     * specified instance.
     * </p>
     * <p>
     * Instance console output is buffered and posted shortly after instance
     * boot, reboot, and termination. Amazon EC2 preserves the most recent 64
     * KB output which will be available for at least one hour after the most
     * recent post.
     * </p>
     *
     * @param getConsoleOutputRequest Container for the necessary parameters
     *           to execute the GetConsoleOutput operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         GetConsoleOutput service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<GetConsoleOutputResult> getConsoleOutputAsync(final GetConsoleOutputRequest getConsoleOutputRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<GetConsoleOutputResult>() {
            public GetConsoleOutputResult call() throws Exception {
                return getConsoleOutput(getConsoleOutputRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The GetConsoleOutput operation retrieves console output for the
     * specified instance.
     * </p>
     * <p>
     * Instance console output is buffered and posted shortly after instance
     * boot, reboot, and termination. Amazon EC2 preserves the most recent 64
     * KB output which will be available for at least one hour after the most
     * recent post.
     * </p>
     *
     * @param getConsoleOutputRequest Container for the necessary parameters
     *           to execute the GetConsoleOutput operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         GetConsoleOutput service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<GetConsoleOutputResult> getConsoleOutputAsync(
            final GetConsoleOutputRequest getConsoleOutputRequest,
            final AsyncHandler<GetConsoleOutputRequest, GetConsoleOutputResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<GetConsoleOutputResult>() {
            public GetConsoleOutputResult call() throws Exception {
            	GetConsoleOutputResult result;
                try {
            		result = getConsoleOutput(getConsoleOutputRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(getConsoleOutputRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The ModifyImageAttribute operation modifies an attribute of an AMI.
     * </p>
     *
     * @param modifyImageAttributeRequest Container for the necessary
     *           parameters to execute the ModifyImageAttribute operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ModifyImageAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> modifyImageAttributeAsync(final ModifyImageAttributeRequest modifyImageAttributeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                modifyImageAttribute(modifyImageAttributeRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * The ModifyImageAttribute operation modifies an attribute of an AMI.
     * </p>
     *
     * @param modifyImageAttributeRequest Container for the necessary
     *           parameters to execute the ModifyImageAttribute operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ModifyImageAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> modifyImageAttributeAsync(
            final ModifyImageAttributeRequest modifyImageAttributeRequest,
            final AsyncHandler<ModifyImageAttributeRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		modifyImageAttribute(modifyImageAttributeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(modifyImageAttributeRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * Creates the data feed for Spot Instances, enabling you to view Spot
     * Instance usage logs. You can create one data feed per account.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide .
     * 
     * </p>
     *
     * @param createSpotDatafeedSubscriptionRequest Container for the
     *           necessary parameters to execute the CreateSpotDatafeedSubscription
     *           operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CreateSpotDatafeedSubscription service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateSpotDatafeedSubscriptionResult> createSpotDatafeedSubscriptionAsync(final CreateSpotDatafeedSubscriptionRequest createSpotDatafeedSubscriptionRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateSpotDatafeedSubscriptionResult>() {
            public CreateSpotDatafeedSubscriptionResult call() throws Exception {
                return createSpotDatafeedSubscription(createSpotDatafeedSubscriptionRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Creates the data feed for Spot Instances, enabling you to view Spot
     * Instance usage logs. You can create one data feed per account.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide .
     * 
     * </p>
     *
     * @param createSpotDatafeedSubscriptionRequest Container for the
     *           necessary parameters to execute the CreateSpotDatafeedSubscription
     *           operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CreateSpotDatafeedSubscription service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateSpotDatafeedSubscriptionResult> createSpotDatafeedSubscriptionAsync(
            final CreateSpotDatafeedSubscriptionRequest createSpotDatafeedSubscriptionRequest,
            final AsyncHandler<CreateSpotDatafeedSubscriptionRequest, CreateSpotDatafeedSubscriptionResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateSpotDatafeedSubscriptionResult>() {
            public CreateSpotDatafeedSubscriptionResult call() throws Exception {
            	CreateSpotDatafeedSubscriptionResult result;
                try {
            		result = createSpotDatafeedSubscription(createSpotDatafeedSubscriptionRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(createSpotDatafeedSubscriptionRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     *
     * @param describeConversionTasksRequest Container for the necessary
     *           parameters to execute the DescribeConversionTasks operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeConversionTasks service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeConversionTasksResult> describeConversionTasksAsync(final DescribeConversionTasksRequest describeConversionTasksRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeConversionTasksResult>() {
            public DescribeConversionTasksResult call() throws Exception {
                return describeConversionTasks(describeConversionTasksRequest);
		    }
		});
    }

    
    /**
     *
     * @param describeConversionTasksRequest Container for the necessary
     *           parameters to execute the DescribeConversionTasks operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeConversionTasks service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeConversionTasksResult> describeConversionTasksAsync(
            final DescribeConversionTasksRequest describeConversionTasksRequest,
            final AsyncHandler<DescribeConversionTasksRequest, DescribeConversionTasksResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeConversionTasksResult>() {
            public DescribeConversionTasksResult call() throws Exception {
            	DescribeConversionTasksResult result;
                try {
            		result = describeConversionTasks(describeConversionTasksRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeConversionTasksRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Returns information about an attribute of an instance. Only one
     * attribute can be specified per call.
     * </p>
     *
     * @param describeInstanceAttributeRequest Container for the necessary
     *           parameters to execute the DescribeInstanceAttribute operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeInstanceAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeInstanceAttributeResult> describeInstanceAttributeAsync(final DescribeInstanceAttributeRequest describeInstanceAttributeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeInstanceAttributeResult>() {
            public DescribeInstanceAttributeResult call() throws Exception {
                return describeInstanceAttribute(describeInstanceAttributeRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Returns information about an attribute of an instance. Only one
     * attribute can be specified per call.
     * </p>
     *
     * @param describeInstanceAttributeRequest Container for the necessary
     *           parameters to execute the DescribeInstanceAttribute operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeInstanceAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeInstanceAttributeResult> describeInstanceAttributeAsync(
            final DescribeInstanceAttributeRequest describeInstanceAttributeRequest,
            final AsyncHandler<DescribeInstanceAttributeRequest, DescribeInstanceAttributeResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeInstanceAttributeResult>() {
            public DescribeInstanceAttributeResult call() throws Exception {
            	DescribeInstanceAttributeResult result;
                try {
            		result = describeInstanceAttribute(describeInstanceAttributeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeInstanceAttributeRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Returns information about one or more PlacementGroup instances in a
     * user's account.
     * </p>
     *
     * @param describePlacementGroupsRequest Container for the necessary
     *           parameters to execute the DescribePlacementGroups operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribePlacementGroups service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribePlacementGroupsResult> describePlacementGroupsAsync(final DescribePlacementGroupsRequest describePlacementGroupsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribePlacementGroupsResult>() {
            public DescribePlacementGroupsResult call() throws Exception {
                return describePlacementGroups(describePlacementGroupsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Returns information about one or more PlacementGroup instances in a
     * user's account.
     * </p>
     *
     * @param describePlacementGroupsRequest Container for the necessary
     *           parameters to execute the DescribePlacementGroups operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribePlacementGroups service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribePlacementGroupsResult> describePlacementGroupsAsync(
            final DescribePlacementGroupsRequest describePlacementGroupsRequest,
            final AsyncHandler<DescribePlacementGroupsRequest, DescribePlacementGroupsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribePlacementGroupsResult>() {
            public DescribePlacementGroupsResult call() throws Exception {
            	DescribePlacementGroupsResult result;
                try {
            		result = describePlacementGroups(describePlacementGroupsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describePlacementGroupsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Gives you information about your subnets. You can filter the results
     * to return information only about subnets that match criteria you
     * specify.
     * </p>
     * <p>
     * For example, you could ask to get information about a particular
     * subnet (or all) only if the subnet's state is available. You can
     * specify multiple filters (e.g., the subnet is in a particular VPC, and
     * the subnet's state is available).
     * </p>
     * <p>
     * The result includes information for a particular subnet only if the
     * subnet matches all your filters. If there's no match, no special
     * message is returned; the response is simply empty. The following table
     * shows the available filters.
     * </p>
     *
     * @param describeSubnetsRequest Container for the necessary parameters
     *           to execute the DescribeSubnets operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSubnets service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSubnetsResult> describeSubnetsAsync(final DescribeSubnetsRequest describeSubnetsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSubnetsResult>() {
            public DescribeSubnetsResult call() throws Exception {
                return describeSubnets(describeSubnetsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Gives you information about your subnets. You can filter the results
     * to return information only about subnets that match criteria you
     * specify.
     * </p>
     * <p>
     * For example, you could ask to get information about a particular
     * subnet (or all) only if the subnet's state is available. You can
     * specify multiple filters (e.g., the subnet is in a particular VPC, and
     * the subnet's state is available).
     * </p>
     * <p>
     * The result includes information for a particular subnet only if the
     * subnet matches all your filters. If there's no match, no special
     * message is returned; the response is simply empty. The following table
     * shows the available filters.
     * </p>
     *
     * @param describeSubnetsRequest Container for the necessary parameters
     *           to execute the DescribeSubnets operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSubnets service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSubnetsResult> describeSubnetsAsync(
            final DescribeSubnetsRequest describeSubnetsRequest,
            final AsyncHandler<DescribeSubnetsRequest, DescribeSubnetsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSubnetsResult>() {
            public DescribeSubnetsResult call() throws Exception {
            	DescribeSubnetsResult result;
                try {
            		result = describeSubnets(describeSubnetsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeSubnetsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The RunInstances operation launches a specified number of instances.
     * </p>
     * <p>
     * If Amazon EC2 cannot launch the minimum number AMIs you request, no
     * instances launch. If there is insufficient capacity to launch the
     * maximum number of AMIs you request, Amazon EC2 launches as many as
     * possible to satisfy the requested maximum values.
     * </p>
     * <p>
     * Every instance is launched in a security group. If you do not specify
     * a security group at launch, the instances start in your default
     * security group. For more information on creating security groups, see
     * CreateSecurityGroup.
     * </p>
     * <p>
     * An optional instance type can be specified. For information about
     * instance types, see Instance Types.
     * </p>
     * <p>
     * You can provide an optional key pair ID for each image in the launch
     * request (for more information, see CreateKeyPair). All instances that
     * are created from images that use this key pair will have access to the
     * associated public key at boot. You can use this key to provide secure
     * access to an instance of an image on a per-instance basis. Amazon EC2
     * public images use this feature to provide secure access without
     * passwords.
     * </p>
     * <p>
     * <b>IMPORTANT:</b> Launching public images without a key pair ID will
     * leave them inaccessible. The public key material is made available to
     * the instance at boot time by placing it in the openssh_id.pub file on
     * a logical device that is exposed to the instance as /dev/sda2 (the
     * ephemeral store). The format of this file is suitable for use as an
     * entry within ~/.ssh/authorized_keys (the OpenSSH format). This can be
     * done at boot (e.g., as part of rc.local) allowing for secure access
     * without passwords. Optional user data can be provided in the launch
     * request. All instances that collectively comprise the launch request
     * have access to this data For more information, see Instance Metadata.
     * </p>
     * <p>
     * <b>NOTE:</b> If any of the AMIs have a product code attached for which
     * the user has not subscribed, the RunInstances call will fail.
     * </p>
     * <p>
     * <b>IMPORTANT:</b> We strongly recommend using the 2.6.18 Xen stock
     * kernel with the c1.medium and c1.xlarge instances. Although the
     * default Amazon EC2 kernels will work, the new kernels provide greater
     * stability and performance for these instance types. For more
     * information about kernels, see Kernels, RAM Disks, and Block Device
     * Mappings.
     * </p>
     *
     * @param runInstancesRequest Container for the necessary parameters to
     *           execute the RunInstances operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         RunInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<RunInstancesResult> runInstancesAsync(final RunInstancesRequest runInstancesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<RunInstancesResult>() {
            public RunInstancesResult call() throws Exception {
                return runInstances(runInstancesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The RunInstances operation launches a specified number of instances.
     * </p>
     * <p>
     * If Amazon EC2 cannot launch the minimum number AMIs you request, no
     * instances launch. If there is insufficient capacity to launch the
     * maximum number of AMIs you request, Amazon EC2 launches as many as
     * possible to satisfy the requested maximum values.
     * </p>
     * <p>
     * Every instance is launched in a security group. If you do not specify
     * a security group at launch, the instances start in your default
     * security group. For more information on creating security groups, see
     * CreateSecurityGroup.
     * </p>
     * <p>
     * An optional instance type can be specified. For information about
     * instance types, see Instance Types.
     * </p>
     * <p>
     * You can provide an optional key pair ID for each image in the launch
     * request (for more information, see CreateKeyPair). All instances that
     * are created from images that use this key pair will have access to the
     * associated public key at boot. You can use this key to provide secure
     * access to an instance of an image on a per-instance basis. Amazon EC2
     * public images use this feature to provide secure access without
     * passwords.
     * </p>
     * <p>
     * <b>IMPORTANT:</b> Launching public images without a key pair ID will
     * leave them inaccessible. The public key material is made available to
     * the instance at boot time by placing it in the openssh_id.pub file on
     * a logical device that is exposed to the instance as /dev/sda2 (the
     * ephemeral store). The format of this file is suitable for use as an
     * entry within ~/.ssh/authorized_keys (the OpenSSH format). This can be
     * done at boot (e.g., as part of rc.local) allowing for secure access
     * without passwords. Optional user data can be provided in the launch
     * request. All instances that collectively comprise the launch request
     * have access to this data For more information, see Instance Metadata.
     * </p>
     * <p>
     * <b>NOTE:</b> If any of the AMIs have a product code attached for which
     * the user has not subscribed, the RunInstances call will fail.
     * </p>
     * <p>
     * <b>IMPORTANT:</b> We strongly recommend using the 2.6.18 Xen stock
     * kernel with the c1.medium and c1.xlarge instances. Although the
     * default Amazon EC2 kernels will work, the new kernels provide greater
     * stability and performance for these instance types. For more
     * information about kernels, see Kernels, RAM Disks, and Block Device
     * Mappings.
     * </p>
     *
     * @param runInstancesRequest Container for the necessary parameters to
     *           execute the RunInstances operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         RunInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<RunInstancesResult> runInstancesAsync(
            final RunInstancesRequest runInstancesRequest,
            final AsyncHandler<RunInstancesRequest, RunInstancesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<RunInstancesResult>() {
            public RunInstancesResult call() throws Exception {
            	RunInstancesResult result;
                try {
            		result = runInstances(runInstancesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(runInstancesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeInstances operation returns information about instances
     * that you own.
     * </p>
     * <p>
     * If you specify one or more instance IDs, Amazon EC2 returns
     * information for those instances. If you do not specify instance IDs,
     * Amazon EC2 returns information for all relevant instances. If you
     * specify an invalid instance ID, a fault is returned. If you specify an
     * instance that you do not own, it will not be included in the returned
     * results.
     * </p>
     * <p>
     * Recently terminated instances might appear in the returned results.
     * This interval is usually less than one hour.
     * </p>
     *
     * @param describeInstancesRequest Container for the necessary parameters
     *           to execute the DescribeInstances operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeInstancesResult> describeInstancesAsync(final DescribeInstancesRequest describeInstancesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeInstancesResult>() {
            public DescribeInstancesResult call() throws Exception {
                return describeInstances(describeInstancesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeInstances operation returns information about instances
     * that you own.
     * </p>
     * <p>
     * If you specify one or more instance IDs, Amazon EC2 returns
     * information for those instances. If you do not specify instance IDs,
     * Amazon EC2 returns information for all relevant instances. If you
     * specify an invalid instance ID, a fault is returned. If you specify an
     * instance that you do not own, it will not be included in the returned
     * results.
     * </p>
     * <p>
     * Recently terminated instances might appear in the returned results.
     * This interval is usually less than one hour.
     * </p>
     *
     * @param describeInstancesRequest Container for the necessary parameters
     *           to execute the DescribeInstances operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeInstancesResult> describeInstancesAsync(
            final DescribeInstancesRequest describeInstancesRequest,
            final AsyncHandler<DescribeInstancesRequest, DescribeInstancesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeInstancesResult>() {
            public DescribeInstancesResult call() throws Exception {
            	DescribeInstancesResult result;
                try {
            		result = describeInstances(describeInstancesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeInstancesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     *
     * @param modifyVolumeAttributeRequest Container for the necessary
     *           parameters to execute the ModifyVolumeAttribute operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ModifyVolumeAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> modifyVolumeAttributeAsync(final ModifyVolumeAttributeRequest modifyVolumeAttributeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                modifyVolumeAttribute(modifyVolumeAttributeRequest);
                return null;
		    }
		});
    }

    
    /**
     *
     * @param modifyVolumeAttributeRequest Container for the necessary
     *           parameters to execute the ModifyVolumeAttribute operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ModifyVolumeAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> modifyVolumeAttributeAsync(
            final ModifyVolumeAttributeRequest modifyVolumeAttributeRequest,
            final AsyncHandler<ModifyVolumeAttributeRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		modifyVolumeAttribute(modifyVolumeAttributeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(modifyVolumeAttributeRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeImages operation returns information about AMIs, AKIs,
     * and ARIs available to the user. Information returned includes image
     * type, product codes, architecture, and kernel and RAM disk IDs. Images
     * available to the user include public images available for any user to
     * launch, private images owned by the user making the request, and
     * private images owned by other users for which the user has explicit
     * launch permissions.
     * </p>
     * <p>
     * Launch permissions fall into three categories:
     * </p>
     * 
     * <ul>
     * <li> <b>Public:</b> The owner of the AMI granted launch permissions
     * for the AMI to the all group. All users have launch permissions for
     * these AMIs. </li>
     * <li> <b>Explicit:</b> The owner of the AMI granted launch permissions
     * to a specific user. </li>
     * <li> <b>Implicit:</b> A user has implicit launch permissions for all
     * AMIs he or she owns. </li>
     * 
     * </ul>
     * <p>
     * The list of AMIs returned can be modified by specifying AMI IDs, AMI
     * owners, or users with launch permissions. If no options are specified,
     * Amazon EC2 returns all AMIs for which the user has launch permissions.
     * </p>
     * <p>
     * If you specify one or more AMI IDs, only AMIs that have the specified
     * IDs are returned. If you specify an invalid AMI ID, a fault is
     * returned. If you specify an AMI ID for which you do not have access,
     * it will not be included in the returned results.
     * </p>
     * <p>
     * If you specify one or more AMI owners, only AMIs from the specified
     * owners and for which you have access are returned. The results can
     * include the account IDs of the specified owners, amazon for AMIs owned
     * by Amazon or self for AMIs that you own.
     * </p>
     * <p>
     * If you specify a list of executable users, only users that have
     * launch permissions for the AMIs are returned. You can specify account
     * IDs (if you own the AMI(s)), self for AMIs for which you own or have
     * explicit permissions, or all for public AMIs.
     * </p>
     * <p>
     * <b>NOTE:</b> Deregistered images are included in the returned results
     * for an unspecified interval after deregistration.
     * </p>
     *
     * @param describeImagesRequest Container for the necessary parameters to
     *           execute the DescribeImages operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeImages service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeImagesResult> describeImagesAsync(final DescribeImagesRequest describeImagesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeImagesResult>() {
            public DescribeImagesResult call() throws Exception {
                return describeImages(describeImagesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeImages operation returns information about AMIs, AKIs,
     * and ARIs available to the user. Information returned includes image
     * type, product codes, architecture, and kernel and RAM disk IDs. Images
     * available to the user include public images available for any user to
     * launch, private images owned by the user making the request, and
     * private images owned by other users for which the user has explicit
     * launch permissions.
     * </p>
     * <p>
     * Launch permissions fall into three categories:
     * </p>
     * 
     * <ul>
     * <li> <b>Public:</b> The owner of the AMI granted launch permissions
     * for the AMI to the all group. All users have launch permissions for
     * these AMIs. </li>
     * <li> <b>Explicit:</b> The owner of the AMI granted launch permissions
     * to a specific user. </li>
     * <li> <b>Implicit:</b> A user has implicit launch permissions for all
     * AMIs he or she owns. </li>
     * 
     * </ul>
     * <p>
     * The list of AMIs returned can be modified by specifying AMI IDs, AMI
     * owners, or users with launch permissions. If no options are specified,
     * Amazon EC2 returns all AMIs for which the user has launch permissions.
     * </p>
     * <p>
     * If you specify one or more AMI IDs, only AMIs that have the specified
     * IDs are returned. If you specify an invalid AMI ID, a fault is
     * returned. If you specify an AMI ID for which you do not have access,
     * it will not be included in the returned results.
     * </p>
     * <p>
     * If you specify one or more AMI owners, only AMIs from the specified
     * owners and for which you have access are returned. The results can
     * include the account IDs of the specified owners, amazon for AMIs owned
     * by Amazon or self for AMIs that you own.
     * </p>
     * <p>
     * If you specify a list of executable users, only users that have
     * launch permissions for the AMIs are returned. You can specify account
     * IDs (if you own the AMI(s)), self for AMIs for which you own or have
     * explicit permissions, or all for public AMIs.
     * </p>
     * <p>
     * <b>NOTE:</b> Deregistered images are included in the returned results
     * for an unspecified interval after deregistration.
     * </p>
     *
     * @param describeImagesRequest Container for the necessary parameters to
     *           execute the DescribeImages operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeImages service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeImagesResult> describeImagesAsync(
            final DescribeImagesRequest describeImagesRequest,
            final AsyncHandler<DescribeImagesRequest, DescribeImagesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeImagesResult>() {
            public DescribeImagesResult call() throws Exception {
            	DescribeImagesResult result;
                try {
            		result = describeImages(describeImagesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeImagesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Starts an instance that uses an Amazon EBS volume as its root device.
     * Instances that use Amazon EBS volumes as their root devices can be
     * quickly stopped and started. When an instance is stopped, the compute
     * resources are released and you are not billed for hourly instance
     * usage. However, your root partition Amazon EBS volume remains,
     * continues to persist your data, and you are charged for Amazon EBS
     * volume usage. You can restart your instance at any time.
     * </p>
     * <p>
     * <b>NOTE:</b> Performing this operation on an instance that uses an
     * instance store as its root device returns an error.
     * </p>
     *
     * @param startInstancesRequest Container for the necessary parameters to
     *           execute the StartInstances operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         StartInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<StartInstancesResult> startInstancesAsync(final StartInstancesRequest startInstancesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<StartInstancesResult>() {
            public StartInstancesResult call() throws Exception {
                return startInstances(startInstancesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Starts an instance that uses an Amazon EBS volume as its root device.
     * Instances that use Amazon EBS volumes as their root devices can be
     * quickly stopped and started. When an instance is stopped, the compute
     * resources are released and you are not billed for hourly instance
     * usage. However, your root partition Amazon EBS volume remains,
     * continues to persist your data, and you are charged for Amazon EBS
     * volume usage. You can restart your instance at any time.
     * </p>
     * <p>
     * <b>NOTE:</b> Performing this operation on an instance that uses an
     * instance store as its root device returns an error.
     * </p>
     *
     * @param startInstancesRequest Container for the necessary parameters to
     *           execute the StartInstances operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         StartInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<StartInstancesResult> startInstancesAsync(
            final StartInstancesRequest startInstancesRequest,
            final AsyncHandler<StartInstancesRequest, StartInstancesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<StartInstancesResult>() {
            public StartInstancesResult call() throws Exception {
            	StartInstancesResult result;
                try {
            		result = startInstances(startInstancesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(startInstancesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     *
     * @param cancelReservedInstancesListingRequest Container for the
     *           necessary parameters to execute the CancelReservedInstancesListing
     *           operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CancelReservedInstancesListing service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CancelReservedInstancesListingResult> cancelReservedInstancesListingAsync(final CancelReservedInstancesListingRequest cancelReservedInstancesListingRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CancelReservedInstancesListingResult>() {
            public CancelReservedInstancesListingResult call() throws Exception {
                return cancelReservedInstancesListing(cancelReservedInstancesListingRequest);
		    }
		});
    }

    
    /**
     *
     * @param cancelReservedInstancesListingRequest Container for the
     *           necessary parameters to execute the CancelReservedInstancesListing
     *           operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CancelReservedInstancesListing service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CancelReservedInstancesListingResult> cancelReservedInstancesListingAsync(
            final CancelReservedInstancesListingRequest cancelReservedInstancesListingRequest,
            final AsyncHandler<CancelReservedInstancesListingRequest, CancelReservedInstancesListingResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CancelReservedInstancesListingResult>() {
            public CancelReservedInstancesListingResult call() throws Exception {
            	CancelReservedInstancesListingResult result;
                try {
            		result = cancelReservedInstancesListing(cancelReservedInstancesListingRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(cancelReservedInstancesListingRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Modifies an attribute of an instance.
     * </p>
     *
     * @param modifyInstanceAttributeRequest Container for the necessary
     *           parameters to execute the ModifyInstanceAttribute operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ModifyInstanceAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> modifyInstanceAttributeAsync(final ModifyInstanceAttributeRequest modifyInstanceAttributeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                modifyInstanceAttribute(modifyInstanceAttributeRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Modifies an attribute of an instance.
     * </p>
     *
     * @param modifyInstanceAttributeRequest Container for the necessary
     *           parameters to execute the ModifyInstanceAttribute operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ModifyInstanceAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> modifyInstanceAttributeAsync(
            final ModifyInstanceAttributeRequest modifyInstanceAttributeRequest,
            final AsyncHandler<ModifyInstanceAttributeRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		modifyInstanceAttribute(modifyInstanceAttributeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(modifyInstanceAttributeRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * The AuthorizeSecurityGroupIngress operation adds permissions to a
     * security group.
     * </p>
     * <p>
     * Permissions are specified by the IP protocol (TCP, UDP or ICMP), the
     * source of the request (by IP range or an Amazon EC2 user-group pair),
     * the source and destination port ranges (for TCP and UDP), and the ICMP
     * codes and types (for ICMP). When authorizing ICMP, <code>-1</code> can
     * be used as a wildcard in the type and code fields.
     * </p>
     * <p>
     * Permission changes are propagated to instances within the security
     * group as quickly as possible. However, depending on the number of
     * instances, a small delay might occur.
     * </p>
     *
     * @param authorizeSecurityGroupIngressRequest Container for the
     *           necessary parameters to execute the AuthorizeSecurityGroupIngress
     *           operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         AuthorizeSecurityGroupIngress service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> authorizeSecurityGroupIngressAsync(final AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * The AuthorizeSecurityGroupIngress operation adds permissions to a
     * security group.
     * </p>
     * <p>
     * Permissions are specified by the IP protocol (TCP, UDP or ICMP), the
     * source of the request (by IP range or an Amazon EC2 user-group pair),
     * the source and destination port ranges (for TCP and UDP), and the ICMP
     * codes and types (for ICMP). When authorizing ICMP, <code>-1</code> can
     * be used as a wildcard in the type and code fields.
     * </p>
     * <p>
     * Permission changes are propagated to instances within the security
     * group as quickly as possible. However, depending on the number of
     * instances, a small delay might occur.
     * </p>
     *
     * @param authorizeSecurityGroupIngressRequest Container for the
     *           necessary parameters to execute the AuthorizeSecurityGroupIngress
     *           operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         AuthorizeSecurityGroupIngress service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> authorizeSecurityGroupIngressAsync(
            final AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest,
            final AsyncHandler<AuthorizeSecurityGroupIngressRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(authorizeSecurityGroupIngressRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * Describes Spot Instance requests. Spot Instances are instances that
     * Amazon EC2 starts on your behalf when the maximum price that you
     * specify exceeds the current Spot Price. Amazon EC2 periodically sets
     * the Spot Price based on available Spot Instance capacity and current
     * spot instance requests. For conceptual information about Spot
     * Instances, refer to the <a
     * "http://docs.amazonwebservices.com/AWSEC2/2010-08-31/DeveloperGuide/">
     * Amazon Elastic Compute Cloud Developer Guide </a> or <a
     * href="http://docs.amazonwebservices.com/AWSEC2/2010-08-31/UserGuide/">
     * Amazon Elastic Compute Cloud User Guide </a> .
     * </p>
     * <p>
     * You can filter the results to return information only about Spot
     * Instance requests that match criteria you specify. For example, you
     * could get information about requests where the Spot Price you
     * specified is a certain value (you can't use greater than or less than
     * comparison, but you can use <code>*</code> and <code>?</code>
     * wildcards). You can specify multiple values for a filter. A Spot
     * Instance request must match at least one of the specified values for
     * it to be included in the results.
     * </p>
     * <p>
     * You can specify multiple filters (e.g., the Spot Price is equal to a
     * particular value, and the instance type is <code>m1.small</code> ).
     * The result includes information for a particular request only if it
     * matches all your filters. If there's no match, no special message is
     * returned; the response is simply empty.
     * </p>
     * <p>
     * You can use wildcards with the filter values: an asterisk matches
     * zero or more characters, and <code>?</code> matches exactly one
     * character. You can escape special characters using a backslash before
     * the character. For example, a value of <code>\*amazon\?\\</code>
     * searches for the literal string <code>*amazon?\</code> .
     * 
     * </p>
     *
     * @param describeSpotInstanceRequestsRequest Container for the necessary
     *           parameters to execute the DescribeSpotInstanceRequests operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSpotInstanceRequests service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSpotInstanceRequestsResult> describeSpotInstanceRequestsAsync(final DescribeSpotInstanceRequestsRequest describeSpotInstanceRequestsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSpotInstanceRequestsResult>() {
            public DescribeSpotInstanceRequestsResult call() throws Exception {
                return describeSpotInstanceRequests(describeSpotInstanceRequestsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Describes Spot Instance requests. Spot Instances are instances that
     * Amazon EC2 starts on your behalf when the maximum price that you
     * specify exceeds the current Spot Price. Amazon EC2 periodically sets
     * the Spot Price based on available Spot Instance capacity and current
     * spot instance requests. For conceptual information about Spot
     * Instances, refer to the <a
     * "http://docs.amazonwebservices.com/AWSEC2/2010-08-31/DeveloperGuide/">
     * Amazon Elastic Compute Cloud Developer Guide </a> or <a
     * href="http://docs.amazonwebservices.com/AWSEC2/2010-08-31/UserGuide/">
     * Amazon Elastic Compute Cloud User Guide </a> .
     * </p>
     * <p>
     * You can filter the results to return information only about Spot
     * Instance requests that match criteria you specify. For example, you
     * could get information about requests where the Spot Price you
     * specified is a certain value (you can't use greater than or less than
     * comparison, but you can use <code>*</code> and <code>?</code>
     * wildcards). You can specify multiple values for a filter. A Spot
     * Instance request must match at least one of the specified values for
     * it to be included in the results.
     * </p>
     * <p>
     * You can specify multiple filters (e.g., the Spot Price is equal to a
     * particular value, and the instance type is <code>m1.small</code> ).
     * The result includes information for a particular request only if it
     * matches all your filters. If there's no match, no special message is
     * returned; the response is simply empty.
     * </p>
     * <p>
     * You can use wildcards with the filter values: an asterisk matches
     * zero or more characters, and <code>?</code> matches exactly one
     * character. You can escape special characters using a backslash before
     * the character. For example, a value of <code>\*amazon\?\\</code>
     * searches for the literal string <code>*amazon?\</code> .
     * 
     * </p>
     *
     * @param describeSpotInstanceRequestsRequest Container for the necessary
     *           parameters to execute the DescribeSpotInstanceRequests operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSpotInstanceRequests service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSpotInstanceRequestsResult> describeSpotInstanceRequestsAsync(
            final DescribeSpotInstanceRequestsRequest describeSpotInstanceRequestsRequest,
            final AsyncHandler<DescribeSpotInstanceRequestsRequest, DescribeSpotInstanceRequestsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSpotInstanceRequestsResult>() {
            public DescribeSpotInstanceRequestsResult call() throws Exception {
            	DescribeSpotInstanceRequestsResult result;
                try {
            		result = describeSpotInstanceRequests(describeSpotInstanceRequestsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeSpotInstanceRequestsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     *
     * @param cancelExportTaskRequest Container for the necessary parameters
     *           to execute the CancelExportTask operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CancelExportTask service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> cancelExportTaskAsync(final CancelExportTaskRequest cancelExportTaskRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                cancelExportTask(cancelExportTaskRequest);
                return null;
		    }
		});
    }

    
    /**
     *
     * @param cancelExportTaskRequest Container for the necessary parameters
     *           to execute the CancelExportTask operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CancelExportTask service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> cancelExportTaskAsync(
            final CancelExportTaskRequest cancelExportTaskRequest,
            final AsyncHandler<CancelExportTaskRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		cancelExportTask(cancelExportTaskRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(cancelExportTaskRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     *
     * @param copyImageRequest Container for the necessary parameters to
     *           execute the CopyImage operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CopyImage service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CopyImageResult> copyImageAsync(final CopyImageRequest copyImageRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CopyImageResult>() {
            public CopyImageResult call() throws Exception {
                return copyImage(copyImageRequest);
		    }
		});
    }

    
    /**
     *
     * @param copyImageRequest Container for the necessary parameters to
     *           execute the CopyImage operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CopyImage service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CopyImageResult> copyImageAsync(
            final CopyImageRequest copyImageRequest,
            final AsyncHandler<CopyImageRequest, CopyImageResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CopyImageResult>() {
            public CopyImageResult call() throws Exception {
            	CopyImageResult result;
                try {
            		result = copyImage(copyImageRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(copyImageRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Creates a Spot Instance request.
     * </p>
     * <p>
     * Spot Instances are instances that Amazon EC2 starts on your behalf
     * when the maximum price that you specify exceeds the current Spot
     * Price. Amazon EC2 periodically sets the Spot Price based on available
     * Spot Instance capacity and current spot instance requests.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide.
     * </p>
     *
     * @param requestSpotInstancesRequest Container for the necessary
     *           parameters to execute the RequestSpotInstances operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         RequestSpotInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<RequestSpotInstancesResult> requestSpotInstancesAsync(final RequestSpotInstancesRequest requestSpotInstancesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<RequestSpotInstancesResult>() {
            public RequestSpotInstancesResult call() throws Exception {
                return requestSpotInstances(requestSpotInstancesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Creates a Spot Instance request.
     * </p>
     * <p>
     * Spot Instances are instances that Amazon EC2 starts on your behalf
     * when the maximum price that you specify exceeds the current Spot
     * Price. Amazon EC2 periodically sets the Spot Price based on available
     * Spot Instance capacity and current spot instance requests.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide.
     * </p>
     *
     * @param requestSpotInstancesRequest Container for the necessary
     *           parameters to execute the RequestSpotInstances operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         RequestSpotInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<RequestSpotInstancesResult> requestSpotInstancesAsync(
            final RequestSpotInstancesRequest requestSpotInstancesRequest,
            final AsyncHandler<RequestSpotInstancesRequest, RequestSpotInstancesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<RequestSpotInstancesResult>() {
            public RequestSpotInstancesResult call() throws Exception {
            	RequestSpotInstancesResult result;
                try {
            		result = requestSpotInstances(requestSpotInstancesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(requestSpotInstancesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Adds or overwrites tags for the specified resources. Each resource
     * can have a maximum of 10 tags. Each tag consists of a key-value pair.
     * Tag keys must be unique per resource.
     * </p>
     *
     * @param createTagsRequest Container for the necessary parameters to
     *           execute the CreateTags operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CreateTags service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> createTagsAsync(final CreateTagsRequest createTagsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                createTags(createTagsRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Adds or overwrites tags for the specified resources. Each resource
     * can have a maximum of 10 tags. Each tag consists of a key-value pair.
     * Tag keys must be unique per resource.
     * </p>
     *
     * @param createTagsRequest Container for the necessary parameters to
     *           execute the CreateTags operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CreateTags service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> createTagsAsync(
            final CreateTagsRequest createTagsRequest,
            final AsyncHandler<CreateTagsRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		createTags(createTagsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(createTagsRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     *
     * @param describeVolumeAttributeRequest Container for the necessary
     *           parameters to execute the DescribeVolumeAttribute operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeVolumeAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeVolumeAttributeResult> describeVolumeAttributeAsync(final DescribeVolumeAttributeRequest describeVolumeAttributeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeVolumeAttributeResult>() {
            public DescribeVolumeAttributeResult call() throws Exception {
                return describeVolumeAttribute(describeVolumeAttributeRequest);
		    }
		});
    }

    
    /**
     *
     * @param describeVolumeAttributeRequest Container for the necessary
     *           parameters to execute the DescribeVolumeAttribute operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeVolumeAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeVolumeAttributeResult> describeVolumeAttributeAsync(
            final DescribeVolumeAttributeRequest describeVolumeAttributeRequest,
            final AsyncHandler<DescribeVolumeAttributeRequest, DescribeVolumeAttributeResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeVolumeAttributeResult>() {
            public DescribeVolumeAttributeResult call() throws Exception {
            	DescribeVolumeAttributeResult result;
                try {
            		result = describeVolumeAttribute(describeVolumeAttributeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeVolumeAttributeRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Describes the tags for the specified resources.
     * </p>
     *
     * @param describeTagsRequest Container for the necessary parameters to
     *           execute the DescribeTags operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeTags service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeTagsResult> describeTagsAsync(final DescribeTagsRequest describeTagsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeTagsResult>() {
            public DescribeTagsResult call() throws Exception {
                return describeTags(describeTagsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Describes the tags for the specified resources.
     * </p>
     *
     * @param describeTagsRequest Container for the necessary parameters to
     *           execute the DescribeTags operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeTags service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeTagsResult> describeTagsAsync(
            final DescribeTagsRequest describeTagsRequest,
            final AsyncHandler<DescribeTagsRequest, DescribeTagsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeTagsResult>() {
            public DescribeTagsResult call() throws Exception {
            	DescribeTagsResult result;
                try {
            		result = describeTags(describeTagsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeTagsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * CancelBundleTask operation cancels a pending or in-progress bundling
     * task. This is an asynchronous call and it make take a while for the
     * task to be canceled. If a task is canceled while it is storing items,
     * there may be parts of the incomplete AMI stored in S3. It is up to the
     * caller to clean up these parts from S3.
     * </p>
     *
     * @param cancelBundleTaskRequest Container for the necessary parameters
     *           to execute the CancelBundleTask operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CancelBundleTask service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CancelBundleTaskResult> cancelBundleTaskAsync(final CancelBundleTaskRequest cancelBundleTaskRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CancelBundleTaskResult>() {
            public CancelBundleTaskResult call() throws Exception {
                return cancelBundleTask(cancelBundleTaskRequest);
		    }
		});
    }

    
    /**
     * <p>
     * CancelBundleTask operation cancels a pending or in-progress bundling
     * task. This is an asynchronous call and it make take a while for the
     * task to be canceled. If a task is canceled while it is storing items,
     * there may be parts of the incomplete AMI stored in S3. It is up to the
     * caller to clean up these parts from S3.
     * </p>
     *
     * @param cancelBundleTaskRequest Container for the necessary parameters
     *           to execute the CancelBundleTask operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CancelBundleTask service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CancelBundleTaskResult> cancelBundleTaskAsync(
            final CancelBundleTaskRequest cancelBundleTaskRequest,
            final AsyncHandler<CancelBundleTaskRequest, CancelBundleTaskResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CancelBundleTaskResult>() {
            public CancelBundleTaskResult call() throws Exception {
            	CancelBundleTaskResult result;
                try {
            		result = cancelBundleTask(cancelBundleTaskRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(cancelBundleTaskRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Cancels one or more Spot Instance requests.
     * </p>
     * <p>
     * Spot Instances are instances that Amazon EC2 starts on your behalf
     * when the maximum price that you specify exceeds the current Spot
     * Price. Amazon EC2 periodically sets the Spot Price based on available
     * Spot Instance capacity and current spot instance requests.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide .
     * 
     * </p>
     *
     * @param cancelSpotInstanceRequestsRequest Container for the necessary
     *           parameters to execute the CancelSpotInstanceRequests operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CancelSpotInstanceRequests service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CancelSpotInstanceRequestsResult> cancelSpotInstanceRequestsAsync(final CancelSpotInstanceRequestsRequest cancelSpotInstanceRequestsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CancelSpotInstanceRequestsResult>() {
            public CancelSpotInstanceRequestsResult call() throws Exception {
                return cancelSpotInstanceRequests(cancelSpotInstanceRequestsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Cancels one or more Spot Instance requests.
     * </p>
     * <p>
     * Spot Instances are instances that Amazon EC2 starts on your behalf
     * when the maximum price that you specify exceeds the current Spot
     * Price. Amazon EC2 periodically sets the Spot Price based on available
     * Spot Instance capacity and current spot instance requests.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide .
     * 
     * </p>
     *
     * @param cancelSpotInstanceRequestsRequest Container for the necessary
     *           parameters to execute the CancelSpotInstanceRequests operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CancelSpotInstanceRequests service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CancelSpotInstanceRequestsResult> cancelSpotInstanceRequestsAsync(
            final CancelSpotInstanceRequestsRequest cancelSpotInstanceRequestsRequest,
            final AsyncHandler<CancelSpotInstanceRequestsRequest, CancelSpotInstanceRequestsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CancelSpotInstanceRequestsResult>() {
            public CancelSpotInstanceRequestsResult call() throws Exception {
            	CancelSpotInstanceRequestsResult result;
                try {
            		result = cancelSpotInstanceRequests(cancelSpotInstanceRequestsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(cancelSpotInstanceRequestsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The PurchaseReservedInstancesOffering operation purchases a Reserved
     * Instance for use with your account. With Amazon EC2 Reserved
     * Instances, you purchase the right to launch Amazon EC2 instances for a
     * period of time (without getting insufficient capacity errors) and pay
     * a lower usage rate for the actual time used.
     * </p>
     *
     * @param purchaseReservedInstancesOfferingRequest Container for the
     *           necessary parameters to execute the PurchaseReservedInstancesOffering
     *           operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         PurchaseReservedInstancesOffering service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<PurchaseReservedInstancesOfferingResult> purchaseReservedInstancesOfferingAsync(final PurchaseReservedInstancesOfferingRequest purchaseReservedInstancesOfferingRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<PurchaseReservedInstancesOfferingResult>() {
            public PurchaseReservedInstancesOfferingResult call() throws Exception {
                return purchaseReservedInstancesOffering(purchaseReservedInstancesOfferingRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The PurchaseReservedInstancesOffering operation purchases a Reserved
     * Instance for use with your account. With Amazon EC2 Reserved
     * Instances, you purchase the right to launch Amazon EC2 instances for a
     * period of time (without getting insufficient capacity errors) and pay
     * a lower usage rate for the actual time used.
     * </p>
     *
     * @param purchaseReservedInstancesOfferingRequest Container for the
     *           necessary parameters to execute the PurchaseReservedInstancesOffering
     *           operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         PurchaseReservedInstancesOffering service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<PurchaseReservedInstancesOfferingResult> purchaseReservedInstancesOfferingAsync(
            final PurchaseReservedInstancesOfferingRequest purchaseReservedInstancesOfferingRequest,
            final AsyncHandler<PurchaseReservedInstancesOfferingRequest, PurchaseReservedInstancesOfferingResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<PurchaseReservedInstancesOfferingResult>() {
            public PurchaseReservedInstancesOfferingResult call() throws Exception {
            	PurchaseReservedInstancesOfferingResult result;
                try {
            		result = purchaseReservedInstancesOffering(purchaseReservedInstancesOfferingRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(purchaseReservedInstancesOfferingRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Adds or remove permission settings for the specified snapshot.
     * </p>
     *
     * @param modifySnapshotAttributeRequest Container for the necessary
     *           parameters to execute the ModifySnapshotAttribute operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ModifySnapshotAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> modifySnapshotAttributeAsync(final ModifySnapshotAttributeRequest modifySnapshotAttributeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                modifySnapshotAttribute(modifySnapshotAttributeRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Adds or remove permission settings for the specified snapshot.
     * </p>
     *
     * @param modifySnapshotAttributeRequest Container for the necessary
     *           parameters to execute the ModifySnapshotAttribute operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ModifySnapshotAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> modifySnapshotAttributeAsync(
            final ModifySnapshotAttributeRequest modifySnapshotAttributeRequest,
            final AsyncHandler<ModifySnapshotAttributeRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		modifySnapshotAttribute(modifySnapshotAttributeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(modifySnapshotAttributeRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeReservedInstancesModifications operation describes
     * modifications made to Reserved Instances in your account.
     * </p>
     *
     * @param describeReservedInstancesModificationsRequest Container for the
     *           necessary parameters to execute the
     *           DescribeReservedInstancesModifications operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeReservedInstancesModifications service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeReservedInstancesModificationsResult> describeReservedInstancesModificationsAsync(final DescribeReservedInstancesModificationsRequest describeReservedInstancesModificationsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeReservedInstancesModificationsResult>() {
            public DescribeReservedInstancesModificationsResult call() throws Exception {
                return describeReservedInstancesModifications(describeReservedInstancesModificationsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeReservedInstancesModifications operation describes
     * modifications made to Reserved Instances in your account.
     * </p>
     *
     * @param describeReservedInstancesModificationsRequest Container for the
     *           necessary parameters to execute the
     *           DescribeReservedInstancesModifications operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeReservedInstancesModifications service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeReservedInstancesModificationsResult> describeReservedInstancesModificationsAsync(
            final DescribeReservedInstancesModificationsRequest describeReservedInstancesModificationsRequest,
            final AsyncHandler<DescribeReservedInstancesModificationsRequest, DescribeReservedInstancesModificationsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeReservedInstancesModificationsResult>() {
            public DescribeReservedInstancesModificationsResult call() throws Exception {
            	DescribeReservedInstancesModificationsResult result;
                try {
            		result = describeReservedInstancesModifications(describeReservedInstancesModificationsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeReservedInstancesModificationsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The TerminateInstances operation shuts down one or more instances.
     * This operation is idempotent; if you terminate an instance more than
     * once, each call will succeed.
     * </p>
     * <p>
     * Terminated instances will remain visible after termination
     * (approximately one hour).
     * </p>
     *
     * @param terminateInstancesRequest Container for the necessary
     *           parameters to execute the TerminateInstances operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         TerminateInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<TerminateInstancesResult> terminateInstancesAsync(final TerminateInstancesRequest terminateInstancesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<TerminateInstancesResult>() {
            public TerminateInstancesResult call() throws Exception {
                return terminateInstances(terminateInstancesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The TerminateInstances operation shuts down one or more instances.
     * This operation is idempotent; if you terminate an instance more than
     * once, each call will succeed.
     * </p>
     * <p>
     * Terminated instances will remain visible after termination
     * (approximately one hour).
     * </p>
     *
     * @param terminateInstancesRequest Container for the necessary
     *           parameters to execute the TerminateInstances operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         TerminateInstances service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<TerminateInstancesResult> terminateInstancesAsync(
            final TerminateInstancesRequest terminateInstancesRequest,
            final AsyncHandler<TerminateInstancesRequest, TerminateInstancesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<TerminateInstancesResult>() {
            public TerminateInstancesResult call() throws Exception {
            	TerminateInstancesResult result;
                try {
            		result = terminateInstances(terminateInstancesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(terminateInstancesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Deletes the data feed for Spot Instances.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide .
     * 
     * </p>
     *
     * @param deleteSpotDatafeedSubscriptionRequest Container for the
     *           necessary parameters to execute the DeleteSpotDatafeedSubscription
     *           operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteSpotDatafeedSubscription service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteSpotDatafeedSubscriptionAsync(final DeleteSpotDatafeedSubscriptionRequest deleteSpotDatafeedSubscriptionRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                deleteSpotDatafeedSubscription(deleteSpotDatafeedSubscriptionRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Deletes the data feed for Spot Instances.
     * </p>
     * <p>
     * For conceptual information about Spot Instances, refer to the Amazon
     * Elastic Compute Cloud Developer Guide or Amazon Elastic Compute Cloud
     * User Guide .
     * 
     * </p>
     *
     * @param deleteSpotDatafeedSubscriptionRequest Container for the
     *           necessary parameters to execute the DeleteSpotDatafeedSubscription
     *           operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteSpotDatafeedSubscription service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteSpotDatafeedSubscriptionAsync(
            final DeleteSpotDatafeedSubscriptionRequest deleteSpotDatafeedSubscriptionRequest,
            final AsyncHandler<DeleteSpotDatafeedSubscriptionRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		deleteSpotDatafeedSubscription(deleteSpotDatafeedSubscriptionRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(deleteSpotDatafeedSubscriptionRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * Returns information about an attribute of a snapshot. Only one
     * attribute can be specified per call.
     * </p>
     *
     * @param describeSnapshotAttributeRequest Container for the necessary
     *           parameters to execute the DescribeSnapshotAttribute operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSnapshotAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSnapshotAttributeResult> describeSnapshotAttributeAsync(final DescribeSnapshotAttributeRequest describeSnapshotAttributeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSnapshotAttributeResult>() {
            public DescribeSnapshotAttributeResult call() throws Exception {
                return describeSnapshotAttribute(describeSnapshotAttributeRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Returns information about an attribute of a snapshot. Only one
     * attribute can be specified per call.
     * </p>
     *
     * @param describeSnapshotAttributeRequest Container for the necessary
     *           parameters to execute the DescribeSnapshotAttribute operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSnapshotAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSnapshotAttributeResult> describeSnapshotAttributeAsync(
            final DescribeSnapshotAttributeRequest describeSnapshotAttributeRequest,
            final AsyncHandler<DescribeSnapshotAttributeRequest, DescribeSnapshotAttributeResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSnapshotAttributeResult>() {
            public DescribeSnapshotAttributeResult call() throws Exception {
            	DescribeSnapshotAttributeResult result;
                try {
            		result = describeSnapshotAttribute(describeSnapshotAttributeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeSnapshotAttributeRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeAddresses operation lists elastic IP addresses assigned
     * to your account.
     * </p>
     *
     * @param describeAddressesRequest Container for the necessary parameters
     *           to execute the DescribeAddresses operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeAddresses service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeAddressesResult> describeAddressesAsync(final DescribeAddressesRequest describeAddressesRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeAddressesResult>() {
            public DescribeAddressesResult call() throws Exception {
                return describeAddresses(describeAddressesRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeAddresses operation lists elastic IP addresses assigned
     * to your account.
     * </p>
     *
     * @param describeAddressesRequest Container for the necessary parameters
     *           to execute the DescribeAddresses operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeAddresses service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeAddressesResult> describeAddressesAsync(
            final DescribeAddressesRequest describeAddressesRequest,
            final AsyncHandler<DescribeAddressesRequest, DescribeAddressesResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeAddressesResult>() {
            public DescribeAddressesResult call() throws Exception {
            	DescribeAddressesResult result;
                try {
            		result = describeAddresses(describeAddressesRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeAddressesRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeImageAttribute operation returns information about an
     * attribute of an AMI. Only one attribute can be specified per call.
     * </p>
     *
     * @param describeImageAttributeRequest Container for the necessary
     *           parameters to execute the DescribeImageAttribute operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeImageAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeImageAttributeResult> describeImageAttributeAsync(final DescribeImageAttributeRequest describeImageAttributeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeImageAttributeResult>() {
            public DescribeImageAttributeResult call() throws Exception {
                return describeImageAttribute(describeImageAttributeRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeImageAttribute operation returns information about an
     * attribute of an AMI. Only one attribute can be specified per call.
     * </p>
     *
     * @param describeImageAttributeRequest Container for the necessary
     *           parameters to execute the DescribeImageAttribute operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeImageAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeImageAttributeResult> describeImageAttributeAsync(
            final DescribeImageAttributeRequest describeImageAttributeRequest,
            final AsyncHandler<DescribeImageAttributeRequest, DescribeImageAttributeResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeImageAttributeResult>() {
            public DescribeImageAttributeResult call() throws Exception {
            	DescribeImageAttributeResult result;
                try {
            		result = describeImageAttribute(describeImageAttributeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeImageAttributeRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeKeyPairs operation returns information about key pairs
     * available to you. If you specify key pairs, information about those
     * key pairs is returned. Otherwise, information for all registered key
     * pairs is returned.
     * </p>
     *
     * @param describeKeyPairsRequest Container for the necessary parameters
     *           to execute the DescribeKeyPairs operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeKeyPairs service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeKeyPairsResult> describeKeyPairsAsync(final DescribeKeyPairsRequest describeKeyPairsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeKeyPairsResult>() {
            public DescribeKeyPairsResult call() throws Exception {
                return describeKeyPairs(describeKeyPairsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeKeyPairs operation returns information about key pairs
     * available to you. If you specify key pairs, information about those
     * key pairs is returned. Otherwise, information for all registered key
     * pairs is returned.
     * </p>
     *
     * @param describeKeyPairsRequest Container for the necessary parameters
     *           to execute the DescribeKeyPairs operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeKeyPairs service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeKeyPairsResult> describeKeyPairsAsync(
            final DescribeKeyPairsRequest describeKeyPairsRequest,
            final AsyncHandler<DescribeKeyPairsRequest, DescribeKeyPairsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeKeyPairsResult>() {
            public DescribeKeyPairsResult call() throws Exception {
            	DescribeKeyPairsResult result;
                try {
            		result = describeKeyPairs(describeKeyPairsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeKeyPairsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The ConfirmProductInstance operation returns true if the specified
     * product code is attached to the specified instance. The operation
     * returns false if the product code is not attached to the instance.
     * </p>
     * <p>
     * The ConfirmProductInstance operation can only be executed by the
     * owner of the AMI. This feature is useful when an AMI owner is
     * providing support and wants to verify whether a user's instance is
     * eligible.
     * </p>
     *
     * @param confirmProductInstanceRequest Container for the necessary
     *           parameters to execute the ConfirmProductInstance operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ConfirmProductInstance service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<ConfirmProductInstanceResult> confirmProductInstanceAsync(final ConfirmProductInstanceRequest confirmProductInstanceRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<ConfirmProductInstanceResult>() {
            public ConfirmProductInstanceResult call() throws Exception {
                return confirmProductInstance(confirmProductInstanceRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The ConfirmProductInstance operation returns true if the specified
     * product code is attached to the specified instance. The operation
     * returns false if the product code is not attached to the instance.
     * </p>
     * <p>
     * The ConfirmProductInstance operation can only be executed by the
     * owner of the AMI. This feature is useful when an AMI owner is
     * providing support and wants to verify whether a user's instance is
     * eligible.
     * </p>
     *
     * @param confirmProductInstanceRequest Container for the necessary
     *           parameters to execute the ConfirmProductInstance operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ConfirmProductInstance service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<ConfirmProductInstanceResult> confirmProductInstanceAsync(
            final ConfirmProductInstanceRequest confirmProductInstanceRequest,
            final AsyncHandler<ConfirmProductInstanceRequest, ConfirmProductInstanceResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<ConfirmProductInstanceResult>() {
            public ConfirmProductInstanceResult call() throws Exception {
            	ConfirmProductInstanceResult result;
                try {
            		result = confirmProductInstance(confirmProductInstanceRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(confirmProductInstanceRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Initializes an empty volume of a given size.
     * </p>
     *
     * @param createVolumeRequest Container for the necessary parameters to
     *           execute the CreateVolume operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CreateVolume service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateVolumeResult> createVolumeAsync(final CreateVolumeRequest createVolumeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateVolumeResult>() {
            public CreateVolumeResult call() throws Exception {
                return createVolume(createVolumeRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Initializes an empty volume of a given size.
     * </p>
     *
     * @param createVolumeRequest Container for the necessary parameters to
     *           execute the CreateVolume operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CreateVolume service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateVolumeResult> createVolumeAsync(
            final CreateVolumeRequest createVolumeRequest,
            final AsyncHandler<CreateVolumeRequest, CreateVolumeResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateVolumeResult>() {
            public CreateVolumeResult call() throws Exception {
            	CreateVolumeResult result;
                try {
            		result = createVolume(createVolumeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(createVolumeRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Describes the status of an Amazon Elastic Compute Cloud (Amazon EC2)
     * instance. Instance status provides information about two types of
     * scheduled events for an instance that may require your attention:
     * </p>
     * 
     * <ul>
     * <li> Scheduled Reboot: When Amazon EC2 determines that an instance
     * must be rebooted, the instance's status will return one of two event
     * codes: <code>system-reboot</code> or <code>instance-reboot</code> .
     * System reboot commonly occurs if certain maintenance or upgrade
     * operations require a reboot of the underlying host that supports an
     * instance. Instance reboot commonly occurs if the instance must be
     * rebooted, rather than the underlying host. Rebooting events include a
     * scheduled start and end time. </li>
     * <li> Scheduled Retirement: When Amazon EC2 determines that an
     * instance must be shut down, the instance's status will return an event
     * code called <code>instance-retirement</code> . Retirement commonly
     * occurs when the underlying host is degraded and must be replaced.
     * Retirement events include a scheduled start and end time. You're also
     * notified by email if one of your instances is set to retiring. The
     * email message indicates when your instance will be permanently
     * retired. </li>
     * 
     * </ul>
     * <p>
     * If your instance is permanently retired, it will not be restarted.
     * You can avoid retirement by manually restarting your instance when its
     * event code is <code>instance-retirement</code> . This ensures that
     * your instance is started on a healthy host.
     * </p>
     * <p>
     * <code>DescribeInstanceStatus</code> returns information only for
     * instances in the running state.
     * </p>
     * <p>
     * You can filter the results to return information only about instances
     * that match criteria you specify. For example, you could get
     * information about instances in a specific Availability Zone. You can
     * specify multiple values for a filter (e.g., more than one Availability
     * Zone). An instance must match at least one of the specified values for
     * it to be included in the results.
     * </p>
     * <p>
     * You can specify multiple filters. An instance must match all the
     * filters for it to be included in the results. If there's no match, no
     * special message is returned; the response is simply empty.
     * </p>
     * <p>
     * You can use wildcards with the filter values: <code>*</code> matches
     * zero or more characters, and <code>?</code> matches exactly one
     * character. You can escape special characters using a backslash before
     * the character. For example, a value of <code>\*amazon\?\\</code>
     * searches for the literal string <code>*amazon?\</code> .
     * 
     * </p>
     * <p>
     * The following filters are available:
     * </p>
     * 
     * <ul>
     * <li> <code>availability-zone</code> - Filter on an instance's
     * availability zone. </li>
     * <li> <code>instance-state-name</code> - Filter on the intended state
     * of the instance, e.g., running. </li>
     * <li> <code>instance-state-code</code> - Filter on the intended state
     * code of the instance, e.g., 16. </li>
     * 
     * </ul>
     *
     * @param describeInstanceStatusRequest Container for the necessary
     *           parameters to execute the DescribeInstanceStatus operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeInstanceStatus service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeInstanceStatusResult> describeInstanceStatusAsync(final DescribeInstanceStatusRequest describeInstanceStatusRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeInstanceStatusResult>() {
            public DescribeInstanceStatusResult call() throws Exception {
                return describeInstanceStatus(describeInstanceStatusRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Describes the status of an Amazon Elastic Compute Cloud (Amazon EC2)
     * instance. Instance status provides information about two types of
     * scheduled events for an instance that may require your attention:
     * </p>
     * 
     * <ul>
     * <li> Scheduled Reboot: When Amazon EC2 determines that an instance
     * must be rebooted, the instance's status will return one of two event
     * codes: <code>system-reboot</code> or <code>instance-reboot</code> .
     * System reboot commonly occurs if certain maintenance or upgrade
     * operations require a reboot of the underlying host that supports an
     * instance. Instance reboot commonly occurs if the instance must be
     * rebooted, rather than the underlying host. Rebooting events include a
     * scheduled start and end time. </li>
     * <li> Scheduled Retirement: When Amazon EC2 determines that an
     * instance must be shut down, the instance's status will return an event
     * code called <code>instance-retirement</code> . Retirement commonly
     * occurs when the underlying host is degraded and must be replaced.
     * Retirement events include a scheduled start and end time. You're also
     * notified by email if one of your instances is set to retiring. The
     * email message indicates when your instance will be permanently
     * retired. </li>
     * 
     * </ul>
     * <p>
     * If your instance is permanently retired, it will not be restarted.
     * You can avoid retirement by manually restarting your instance when its
     * event code is <code>instance-retirement</code> . This ensures that
     * your instance is started on a healthy host.
     * </p>
     * <p>
     * <code>DescribeInstanceStatus</code> returns information only for
     * instances in the running state.
     * </p>
     * <p>
     * You can filter the results to return information only about instances
     * that match criteria you specify. For example, you could get
     * information about instances in a specific Availability Zone. You can
     * specify multiple values for a filter (e.g., more than one Availability
     * Zone). An instance must match at least one of the specified values for
     * it to be included in the results.
     * </p>
     * <p>
     * You can specify multiple filters. An instance must match all the
     * filters for it to be included in the results. If there's no match, no
     * special message is returned; the response is simply empty.
     * </p>
     * <p>
     * You can use wildcards with the filter values: <code>*</code> matches
     * zero or more characters, and <code>?</code> matches exactly one
     * character. You can escape special characters using a backslash before
     * the character. For example, a value of <code>\*amazon\?\\</code>
     * searches for the literal string <code>*amazon?\</code> .
     * 
     * </p>
     * <p>
     * The following filters are available:
     * </p>
     * 
     * <ul>
     * <li> <code>availability-zone</code> - Filter on an instance's
     * availability zone. </li>
     * <li> <code>instance-state-name</code> - Filter on the intended state
     * of the instance, e.g., running. </li>
     * <li> <code>instance-state-code</code> - Filter on the intended state
     * code of the instance, e.g., 16. </li>
     * 
     * </ul>
     *
     * @param describeInstanceStatusRequest Container for the necessary
     *           parameters to execute the DescribeInstanceStatus operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeInstanceStatus service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeInstanceStatusResult> describeInstanceStatusAsync(
            final DescribeInstanceStatusRequest describeInstanceStatusRequest,
            final AsyncHandler<DescribeInstanceStatusRequest, DescribeInstanceStatusResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeInstanceStatusResult>() {
            public DescribeInstanceStatusResult call() throws Exception {
            	DescribeInstanceStatusResult result;
                try {
            		result = describeInstanceStatus(describeInstanceStatusRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeInstanceStatusRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The DescribeReservedInstancesOfferings operation describes Reserved
     * Instance offerings that are available for purchase. With Amazon EC2
     * Reserved Instances, you purchase the right to launch Amazon EC2
     * instances for a period of time (without getting insufficient capacity
     * errors) and pay a lower usage rate for the actual time used.
     * </p>
     *
     * @param describeReservedInstancesOfferingsRequest Container for the
     *           necessary parameters to execute the DescribeReservedInstancesOfferings
     *           operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeReservedInstancesOfferings service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeReservedInstancesOfferingsResult> describeReservedInstancesOfferingsAsync(final DescribeReservedInstancesOfferingsRequest describeReservedInstancesOfferingsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeReservedInstancesOfferingsResult>() {
            public DescribeReservedInstancesOfferingsResult call() throws Exception {
                return describeReservedInstancesOfferings(describeReservedInstancesOfferingsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The DescribeReservedInstancesOfferings operation describes Reserved
     * Instance offerings that are available for purchase. With Amazon EC2
     * Reserved Instances, you purchase the right to launch Amazon EC2
     * instances for a period of time (without getting insufficient capacity
     * errors) and pay a lower usage rate for the actual time used.
     * </p>
     *
     * @param describeReservedInstancesOfferingsRequest Container for the
     *           necessary parameters to execute the DescribeReservedInstancesOfferings
     *           operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeReservedInstancesOfferings service method, as returned by
     *         AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeReservedInstancesOfferingsResult> describeReservedInstancesOfferingsAsync(
            final DescribeReservedInstancesOfferingsRequest describeReservedInstancesOfferingsRequest,
            final AsyncHandler<DescribeReservedInstancesOfferingsRequest, DescribeReservedInstancesOfferingsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeReservedInstancesOfferingsResult>() {
            public DescribeReservedInstancesOfferingsResult call() throws Exception {
            	DescribeReservedInstancesOfferingsResult result;
                try {
            		result = describeReservedInstancesOfferings(describeReservedInstancesOfferingsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeReservedInstancesOfferingsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Deletes the snapshot identified by <code>snapshotId</code> .
     * 
     * </p>
     *
     * @param deleteSnapshotRequest Container for the necessary parameters to
     *           execute the DeleteSnapshot operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteSnapshot service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteSnapshotAsync(final DeleteSnapshotRequest deleteSnapshotRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                deleteSnapshot(deleteSnapshotRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Deletes the snapshot identified by <code>snapshotId</code> .
     * 
     * </p>
     *
     * @param deleteSnapshotRequest Container for the necessary parameters to
     *           execute the DeleteSnapshot operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DeleteSnapshot service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deleteSnapshotAsync(
            final DeleteSnapshotRequest deleteSnapshotRequest,
            final AsyncHandler<DeleteSnapshotRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		deleteSnapshot(deleteSnapshotRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(deleteSnapshotRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * The DisassociateAddress operation disassociates the specified elastic
     * IP address from the instance to which it is assigned. This is an
     * idempotent operation. If you enter it more than once, Amazon EC2 does
     * not return an error.
     * </p>
     *
     * @param disassociateAddressRequest Container for the necessary
     *           parameters to execute the DisassociateAddress operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DisassociateAddress service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> disassociateAddressAsync(final DisassociateAddressRequest disassociateAddressRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                disassociateAddress(disassociateAddressRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * The DisassociateAddress operation disassociates the specified elastic
     * IP address from the instance to which it is assigned. This is an
     * idempotent operation. If you enter it more than once, Amazon EC2 does
     * not return an error.
     * </p>
     *
     * @param disassociateAddressRequest Container for the necessary
     *           parameters to execute the DisassociateAddress operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DisassociateAddress service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> disassociateAddressAsync(
            final DisassociateAddressRequest disassociateAddressRequest,
            final AsyncHandler<DisassociateAddressRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		disassociateAddress(disassociateAddressRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(disassociateAddressRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * Creates a PlacementGroup into which multiple Amazon EC2 instances can
     * be launched. Users must give the group a name unique within the scope
     * of the user account.
     * </p>
     *
     * @param createPlacementGroupRequest Container for the necessary
     *           parameters to execute the CreatePlacementGroup operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CreatePlacementGroup service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> createPlacementGroupAsync(final CreatePlacementGroupRequest createPlacementGroupRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                createPlacementGroup(createPlacementGroupRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Creates a PlacementGroup into which multiple Amazon EC2 instances can
     * be launched. Users must give the group a name unique within the scope
     * of the user account.
     * </p>
     *
     * @param createPlacementGroupRequest Container for the necessary
     *           parameters to execute the CreatePlacementGroup operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CreatePlacementGroup service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> createPlacementGroupAsync(
            final CreatePlacementGroupRequest createPlacementGroupRequest,
            final AsyncHandler<CreatePlacementGroupRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		createPlacementGroup(createPlacementGroupRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(createPlacementGroupRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * The BundleInstance operation request that an instance is bundled the
     * next time it boots. The bundling process creates a new image from a
     * running instance and stores the AMI data in S3. Once bundled, the
     * image must be registered in the normal way using the RegisterImage
     * API.
     * </p>
     *
     * @param bundleInstanceRequest Container for the necessary parameters to
     *           execute the BundleInstance operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         BundleInstance service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<BundleInstanceResult> bundleInstanceAsync(final BundleInstanceRequest bundleInstanceRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<BundleInstanceResult>() {
            public BundleInstanceResult call() throws Exception {
                return bundleInstance(bundleInstanceRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The BundleInstance operation request that an instance is bundled the
     * next time it boots. The bundling process creates a new image from a
     * running instance and stores the AMI data in S3. Once bundled, the
     * image must be registered in the normal way using the RegisterImage
     * API.
     * </p>
     *
     * @param bundleInstanceRequest Container for the necessary parameters to
     *           execute the BundleInstance operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         BundleInstance service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<BundleInstanceResult> bundleInstanceAsync(
            final BundleInstanceRequest bundleInstanceRequest,
            final AsyncHandler<BundleInstanceRequest, BundleInstanceResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<BundleInstanceResult>() {
            public BundleInstanceResult call() throws Exception {
            	BundleInstanceResult result;
                try {
            		result = bundleInstance(bundleInstanceRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(bundleInstanceRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Deletes a PlacementGroup from a user's account. Terminate all Amazon
     * EC2 instances in the placement group before deletion.
     * </p>
     *
     * @param deletePlacementGroupRequest Container for the necessary
     *           parameters to execute the DeletePlacementGroup operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DeletePlacementGroup service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deletePlacementGroupAsync(final DeletePlacementGroupRequest deletePlacementGroupRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                deletePlacementGroup(deletePlacementGroupRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Deletes a PlacementGroup from a user's account. Terminate all Amazon
     * EC2 instances in the placement group before deletion.
     * </p>
     *
     * @param deletePlacementGroupRequest Container for the necessary
     *           parameters to execute the DeletePlacementGroup operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DeletePlacementGroup service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> deletePlacementGroupAsync(
            final DeletePlacementGroupRequest deletePlacementGroupRequest,
            final AsyncHandler<DeletePlacementGroupRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		deletePlacementGroup(deletePlacementGroupRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(deletePlacementGroupRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     *
     * @param copySnapshotRequest Container for the necessary parameters to
     *           execute the CopySnapshot operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CopySnapshot service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CopySnapshotResult> copySnapshotAsync(final CopySnapshotRequest copySnapshotRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CopySnapshotResult>() {
            public CopySnapshotResult call() throws Exception {
                return copySnapshot(copySnapshotRequest);
		    }
		});
    }

    
    /**
     *
     * @param copySnapshotRequest Container for the necessary parameters to
     *           execute the CopySnapshot operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CopySnapshot service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CopySnapshotResult> copySnapshotAsync(
            final CopySnapshotRequest copySnapshotRequest,
            final AsyncHandler<CopySnapshotRequest, CopySnapshotResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CopySnapshotResult>() {
            public CopySnapshotResult call() throws Exception {
            	CopySnapshotResult result;
                try {
            		result = copySnapshot(copySnapshotRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(copySnapshotRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The AllocateAddress operation acquires an elastic IP address for use
     * with your account.
     * </p>
     *
     * @param allocateAddressRequest Container for the necessary parameters
     *           to execute the AllocateAddress operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         AllocateAddress service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<AllocateAddressResult> allocateAddressAsync(final AllocateAddressRequest allocateAddressRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<AllocateAddressResult>() {
            public AllocateAddressResult call() throws Exception {
                return allocateAddress(allocateAddressRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The AllocateAddress operation acquires an elastic IP address for use
     * with your account.
     * </p>
     *
     * @param allocateAddressRequest Container for the necessary parameters
     *           to execute the AllocateAddress operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         AllocateAddress service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<AllocateAddressResult> allocateAddressAsync(
            final AllocateAddressRequest allocateAddressRequest,
            final AsyncHandler<AllocateAddressRequest, AllocateAddressResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<AllocateAddressResult>() {
            public AllocateAddressResult call() throws Exception {
            	AllocateAddressResult result;
                try {
            		result = allocateAddress(allocateAddressRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(allocateAddressRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The ReleaseAddress operation releases an elastic IP address
     * associated with your account.
     * </p>
     * <p>
     * <b>NOTE:</b> Releasing an IP address automatically disassociates it
     * from any instance with which it is associated. For more information,
     * see DisassociateAddress.
     * </p>
     * <p>
     * <b>IMPORTANT:</b> After releasing an elastic IP address, it is
     * released to the IP address pool and might no longer be available to
     * your account. Make sure to update your DNS records and any servers or
     * devices that communicate with the address. If you run this operation
     * on an elastic IP address that is already released, the address might
     * be assigned to another account which will cause Amazon EC2 to return
     * an error.
     * </p>
     *
     * @param releaseAddressRequest Container for the necessary parameters to
     *           execute the ReleaseAddress operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ReleaseAddress service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> releaseAddressAsync(final ReleaseAddressRequest releaseAddressRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                releaseAddress(releaseAddressRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * The ReleaseAddress operation releases an elastic IP address
     * associated with your account.
     * </p>
     * <p>
     * <b>NOTE:</b> Releasing an IP address automatically disassociates it
     * from any instance with which it is associated. For more information,
     * see DisassociateAddress.
     * </p>
     * <p>
     * <b>IMPORTANT:</b> After releasing an elastic IP address, it is
     * released to the IP address pool and might no longer be available to
     * your account. Make sure to update your DNS records and any servers or
     * devices that communicate with the address. If you run this operation
     * on an elastic IP address that is already released, the address might
     * be assigned to another account which will cause Amazon EC2 to return
     * an error.
     * </p>
     *
     * @param releaseAddressRequest Container for the necessary parameters to
     *           execute the ReleaseAddress operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ReleaseAddress service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> releaseAddressAsync(
            final ReleaseAddressRequest releaseAddressRequest,
            final AsyncHandler<ReleaseAddressRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		releaseAddress(releaseAddressRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(releaseAddressRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * Resets an attribute of an instance to its default value.
     * </p>
     *
     * @param resetInstanceAttributeRequest Container for the necessary
     *           parameters to execute the ResetInstanceAttribute operation on
     *           AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         ResetInstanceAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> resetInstanceAttributeAsync(final ResetInstanceAttributeRequest resetInstanceAttributeRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
                resetInstanceAttribute(resetInstanceAttributeRequest);
                return null;
		    }
		});
    }

    
    /**
     * <p>
     * Resets an attribute of an instance to its default value.
     * </p>
     *
     * @param resetInstanceAttributeRequest Container for the necessary
     *           parameters to execute the ResetInstanceAttribute operation on
     *           AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         ResetInstanceAttribute service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<Void> resetInstanceAttributeAsync(
            final ResetInstanceAttributeRequest resetInstanceAttributeRequest,
            final AsyncHandler<ResetInstanceAttributeRequest, Void> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<Void>() {
            public Void call() throws Exception {
            	try {
            		resetInstanceAttribute(resetInstanceAttributeRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(resetInstanceAttributeRequest, null);
               	return null;
		    }
		});
    }
    
    /**
     * <p>
     * The CreateKeyPair operation creates a new 2048 bit RSA key pair and
     * returns a unique ID that can be used to reference this key pair when
     * launching new instances. For more information, see RunInstances.
     * </p>
     *
     * @param createKeyPairRequest Container for the necessary parameters to
     *           execute the CreateKeyPair operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         CreateKeyPair service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateKeyPairResult> createKeyPairAsync(final CreateKeyPairRequest createKeyPairRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateKeyPairResult>() {
            public CreateKeyPairResult call() throws Exception {
                return createKeyPair(createKeyPairRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The CreateKeyPair operation creates a new 2048 bit RSA key pair and
     * returns a unique ID that can be used to reference this key pair when
     * launching new instances. For more information, see RunInstances.
     * </p>
     *
     * @param createKeyPairRequest Container for the necessary parameters to
     *           execute the CreateKeyPair operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         CreateKeyPair service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<CreateKeyPairResult> createKeyPairAsync(
            final CreateKeyPairRequest createKeyPairRequest,
            final AsyncHandler<CreateKeyPairRequest, CreateKeyPairResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<CreateKeyPairResult>() {
            public CreateKeyPairResult call() throws Exception {
            	CreateKeyPairResult result;
                try {
            		result = createKeyPair(createKeyPairRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(createKeyPairRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * Returns information about the Amazon EBS snapshots available to you.
     * Snapshots available to you include public snapshots available for any
     * AWS account to launch, private snapshots you own, and private
     * snapshots owned by another AWS account but for which you've been given
     * explicit create volume permissions.
     * </p>
     *
     * @param describeSnapshotsRequest Container for the necessary parameters
     *           to execute the DescribeSnapshots operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSnapshots service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSnapshotsResult> describeSnapshotsAsync(final DescribeSnapshotsRequest describeSnapshotsRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSnapshotsResult>() {
            public DescribeSnapshotsResult call() throws Exception {
                return describeSnapshots(describeSnapshotsRequest);
		    }
		});
    }

    
    /**
     * <p>
     * Returns information about the Amazon EBS snapshots available to you.
     * Snapshots available to you include public snapshots available for any
     * AWS account to launch, private snapshots you own, and private
     * snapshots owned by another AWS account but for which you've been given
     * explicit create volume permissions.
     * </p>
     *
     * @param describeSnapshotsRequest Container for the necessary parameters
     *           to execute the DescribeSnapshots operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         DescribeSnapshots service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<DescribeSnapshotsResult> describeSnapshotsAsync(
            final DescribeSnapshotsRequest describeSnapshotsRequest,
            final AsyncHandler<DescribeSnapshotsRequest, DescribeSnapshotsResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<DescribeSnapshotsResult>() {
            public DescribeSnapshotsResult call() throws Exception {
            	DescribeSnapshotsResult result;
                try {
            		result = describeSnapshots(describeSnapshotsRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(describeSnapshotsRequest, result);
               	return result;
		    }
		});
    }
    
    /**
     * <p>
     * The RegisterImage operation registers an AMI with Amazon EC2. Images
     * must be registered before they can be launched. For more information,
     * see RunInstances.
     * </p>
     * <p>
     * Each AMI is associated with an unique ID which is provided by the
     * Amazon EC2 service through the RegisterImage operation. During
     * registration, Amazon EC2 retrieves the specified image manifest from
     * Amazon S3 and verifies that the image is owned by the user registering
     * the image.
     * </p>
     * <p>
     * The image manifest is retrieved once and stored within the Amazon
     * EC2. Any modifications to an image in Amazon S3 invalidates this
     * registration. If you make changes to an image, deregister the previous
     * image and register the new image. For more information, see
     * DeregisterImage.
     * </p>
     *
     * @param registerImageRequest Container for the necessary parameters to
     *           execute the RegisterImage operation on AmazonEC2.
     * 
     * @return A Java Future object containing the response from the
     *         RegisterImage service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<RegisterImageResult> registerImageAsync(final RegisterImageRequest registerImageRequest) 
            throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<RegisterImageResult>() {
            public RegisterImageResult call() throws Exception {
                return registerImage(registerImageRequest);
		    }
		});
    }

    
    /**
     * <p>
     * The RegisterImage operation registers an AMI with Amazon EC2. Images
     * must be registered before they can be launched. For more information,
     * see RunInstances.
     * </p>
     * <p>
     * Each AMI is associated with an unique ID which is provided by the
     * Amazon EC2 service through the RegisterImage operation. During
     * registration, Amazon EC2 retrieves the specified image manifest from
     * Amazon S3 and verifies that the image is owned by the user registering
     * the image.
     * </p>
     * <p>
     * The image manifest is retrieved once and stored within the Amazon
     * EC2. Any modifications to an image in Amazon S3 invalidates this
     * registration. If you make changes to an image, deregister the previous
     * image and register the new image. For more information, see
     * DeregisterImage.
     * </p>
     *
     * @param registerImageRequest Container for the necessary parameters to
     *           execute the RegisterImage operation on AmazonEC2.
     * @param asyncHandler Asynchronous callback handler for events in the
     *           life-cycle of the request. Users could provide the implementation of
     *           the four callback methods in this interface to process the operation
     *           result or handle the exception.
     * 
     * @return A Java Future object containing the response from the
     *         RegisterImage service method, as returned by AmazonEC2.
     *
     * @throws AmazonClientException
     *             If any internal errors are encountered inside the client while
     *             attempting to make the request or handle the response.  For example
     *             if a network connection is not available.
     * @throws AmazonServiceException
     *             If an error response is returned by AmazonEC2 indicating
     *             either a problem with the data in the request, or a server side issue.
     */
    public Future<RegisterImageResult> registerImageAsync(
            final RegisterImageRequest registerImageRequest,
            final AsyncHandler<RegisterImageRequest, RegisterImageResult> asyncHandler)
                    throws AmazonServiceException, AmazonClientException {
        return executorService.submit(new Callable<RegisterImageResult>() {
            public RegisterImageResult call() throws Exception {
            	RegisterImageResult result;
                try {
            		result = registerImage(registerImageRequest);
            	} catch (Exception ex) {
            	    asyncHandler.onError(ex);
    				throw ex;
            	}
            	asyncHandler.onSuccess(registerImageRequest, result);
               	return result;
		    }
		});
    }
    
}
        
