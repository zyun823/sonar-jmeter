/*
 * JMeter Report Server
 * Copyright (C) 2010 eXcentia
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package es.excentia.jmeter.report.server.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.excentia.jmeter.report.client.serialization.StreamReader;
import es.excentia.jmeter.report.server.data.ConfigInfo;
import es.excentia.jmeter.report.server.exception.JTLFileNotFoundException;
import es.excentia.jmeter.report.server.service.ConfigService;
import es.excentia.jmeter.report.server.service.ReaderService;
import es.excentia.jmeter.report.server.service.ServiceFactory;
import es.excentia.jmeter.report.server.testresults.JtlAbstractSampleReader;
import es.excentia.jmeter.report.server.testresults.JtlHttpSampleReader;
import es.excentia.jmeter.report.server.testresults.JtlSampleMixReader;
import es.excentia.jmeter.report.server.testresults.SampleMix;
import es.excentia.jmeter.report.server.testresults.xmlbeans.AbstractSample;

public class ReaderServiceImpl implements ReaderService {

  private static final String CLASSPATH_PREFIX = "classpath:";
  
  private static final Logger LOG = LoggerFactory.getLogger(ReaderServiceImpl.class);
  private static final boolean LOG_DEBUG =  LOG.isDebugEnabled();
  
  ConfigService configService = ServiceFactory.get(ConfigService.class);
  

  protected InputStream getInputStreamByTestConfig(String config, String jtlPath) {
    
    if (LOG_DEBUG) {
      LOG.debug("getInputStreamByTestConfig for config '" + config+"'");
    }

    
    InputStream is = null;
    
    if (LOG_DEBUG) {
      LOG.debug("JTL path: " + jtlPath);
    }
    
    if (jtlPath.startsWith(CLASSPATH_PREFIX)) {
      
      // The resource is in the classpath
      is = getClass().getResourceAsStream(jtlPath.substring(CLASSPATH_PREFIX.length()));
      if (is==null) {
        throw new JTLFileNotFoundException(config, jtlPath);
      }
    } else {
      
      // The path is a file system path
      try {
        is = new FileInputStream(new File(jtlPath));
      } catch (FileNotFoundException e) {
        throw new JTLFileNotFoundException(config, jtlPath);
      }
      
    }

    return is;
  }

  public StreamReader<AbstractSample> getAbstractSampleReaderByTestConfig(String config) {
    ConfigInfo configInfo = configService.getTestConfigInfo(config);
    InputStream is = getInputStreamByTestConfig(config, configInfo.getJtlPath());
    return new JtlAbstractSampleReader(is, configInfo.getGrowingJtlWaitTime());
  }

  public StreamReader<AbstractSample> getHttpSampleReaderByTestConfig(String config) {
    ConfigInfo configInfo = configService.getTestConfigInfo(config);
    InputStream is = getInputStreamByTestConfig(config, configInfo.getJtlPath());
    return new JtlHttpSampleReader(is, configInfo.getGrowingJtlWaitTime());
  }

  public StreamReader<SampleMix> getSampleMixReaderByTestConfig(String config) {
    ConfigInfo configInfo = configService.getTestConfigInfo(config);
    InputStream is = getInputStreamByTestConfig(config, configInfo.getJtlPath());
    return new JtlSampleMixReader(is, configInfo.getGrowingJtlWaitTime());
  }

}
