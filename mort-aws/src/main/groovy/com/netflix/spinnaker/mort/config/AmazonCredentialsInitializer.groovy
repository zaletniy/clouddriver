/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.mort.config

import com.amazonaws.auth.AWSCredentialsProvider
import com.netflix.spinnaker.amos.AccountCredentialsRepository
import com.netflix.spinnaker.amos.aws.AmazonCredentials
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnExpression('!${bastion.enabled:false}')
class AmazonCredentialsInitializer implements CredentialsInitializer {
  @Autowired
  AWSCredentialsProvider awsCredentialsProvider

  @Autowired
  AccountCredentialsRepository accountCredentialsRepository

  @Autowired
  AwsConfigurationProperties awsConfigurationProperties

  @Value('${default.account.env:default}')
  String defaultEnv

  @PostConstruct
  void init() {
    if (!awsConfigurationProperties.accounts) {
      accountCredentialsRepository.save(defaultEnv, new AmazonCredentials(name: defaultEnv))
    } else {
      for (account in awsConfigurationProperties.accounts) {
        account.credentialsProvider = awsCredentialsProvider
        account.assumeRole = awsConfigurationProperties.assumeRole
        accountCredentialsRepository.save(account.name, account)
      }
    }
  }
}