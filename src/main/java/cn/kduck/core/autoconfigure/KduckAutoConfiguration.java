package cn.kduck.core.autoconfigure;

import cn.kduck.core.remote.scan.RemoteServiceScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@ComponentScan("cn.kduck")
public class KduckAutoConfiguration {

}
