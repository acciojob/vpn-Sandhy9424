package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception {
        User user = userRepository2.findById(userId).get();
        if (user.getConnected() == true) {
            throw new Exception("Already connected");
        } else if (user.getOriginalCountry().toString().equalsIgnoreCase(countryName)) {
            return user;
        } else {
            if (user.getServiceProviderList() == null) {
                throw new Exception("Unable to connect");
            }
            List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
            ServiceProvider tempService = null;
            int minId = Integer.MAX_VALUE;
            String code = "";
            for (ServiceProvider s : serviceProviderList) {
                List<Country> countries = s.getCountryList();
                for (Country c : countries) {
                    if (c.getCountryName().toString().equalsIgnoreCase(countryName)) {
                        if (s.getId() < minId) {
                            tempService = s;
                            minId = s.getId();
                            code = c.getCode();
                        }
                    }
                }
            }
            if (tempService != null) {
                Connection connection = new Connection();
                connection.setUser(user);
                connection.setServiceProvider(tempService);
                user.setConnected(true);
                user.setMaskedIp(code + "." + tempService.getId() + "." + userId);
                List<Connection> connectionList = user.getConnectionList();
                connectionList.add(connection);
                List<Connection> connections = tempService.getConnectionList();
                connections.add(connection);
                serviceProviderRepository2.save(tempService);
                userRepository2.save(user);
            }
            return user;
        }
    }
    @Override
    public User disconnect(int userId) throws Exception {
            User user=userRepository2.findById(userId).get();
            if(user.getConnected()==false){
                throw new Exception("Already disconnected");
            }
            user.setMaskedIp(null);
            user.setConnected(false);
            userRepository2.save(user);
            return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User user = userRepository2.findById(senderId).get();
        User user1 = userRepository2.findById(receiverId).get();

        if(user1.getMaskedIp()!=null){
            String str = user1.getMaskedIp();
            String countryCode = str.substring(0,3);

            if(countryCode.equals(user.getOriginalCountry().getCode()))
                return user;
            else {
                String countryName = "";

                if (countryCode.equalsIgnoreCase(CountryName.IND.toCode()))
                    countryName = CountryName.IND.toString();
                if (countryCode.equalsIgnoreCase(CountryName.USA.toCode()))
                    countryName = CountryName.USA.toString();
                if (countryCode.equalsIgnoreCase(CountryName.JPN.toCode()))
                    countryName = CountryName.JPN.toString();
                if (countryCode.equalsIgnoreCase(CountryName.CHI.toCode()))
                    countryName = CountryName.CHI.toString();
                if (countryCode.equalsIgnoreCase(CountryName.AUS.toCode()))
                    countryName = CountryName.AUS.toString();

                User user2 = connect(senderId,countryName);
                if (!user2.getConnected()){
                    throw new Exception("Cannot establish communication");

                }
                else return user2;
            }

        }
        else {
            if (user1.getOriginalCountry().equals(user.getOriginalCountry())) {
                return user;
            }
            String countryName = user1.getOriginalCountry().getCountryName().toString();
            User user2 = connect(senderId, countryName);
            if (!user2.getConnected()) {
                throw new Exception("Cannot establish communication");
            } else return user2;
        }
    }
}
