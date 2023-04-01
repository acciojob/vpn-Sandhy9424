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
    public User connect(int userId, String countryName) throws Exception{
       User user=userRepository2.findById(userId).get();
       if(user.getConnected()==true){
           throw new Exception("Already connected");
       } else if (user.getOriginalCountry().toString().equalsIgnoreCase(countryName)) {
           return user;
       }
       else{
           List<ServiceProvider>serviceProviderList=user.getServiceProviderList();
           if (serviceProviderList.isEmpty()){
               throw new Exception("Unable to connect");
           }
           ServiceProvider tempService=null;
           int minId=Integer.MAX_VALUE;
           String code="";
           for(ServiceProvider s:serviceProviderList){
               List<Country>countries=s.getCountryList();
               for(Country c:countries){
                   if(c.getCountryName().toString().equalsIgnoreCase(countryName)){
                       if(s.getId()<minId) {
                           tempService = s;
                           minId=s.getId();
                           code=c.getCode();
                       }
                   }
               }
           }
           if(tempService==null){
               throw new Exception("Unable to connect");
           }
           Connection connection=new Connection();
           connection.setUser(user);
           connection.setServiceProvider(tempService);
           user.setConnected(true);
           user.setMaskedIp(code+"."+tempService.getId()+"."+userId);
           List<Connection>connectionList=user.getConnectionList();
           connectionList.add(connection);
           List<Connection>connections=tempService.getConnectionList();
           connections.add(connection);
           serviceProviderRepository2.save(tempService);
           userRepository2.save(user);
       }
      return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
            User user=userRepository2.findById(userId).get();
            if(user.getConnected()==false){
                throw new Exception("Already disconnected");
            }
            user.setMaskedIp(null);
            user.setConnected(false);
            return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender=userRepository2.findById(senderId).get();
        User receiver=userRepository2.findById(receiverId).get();
        if(receiver.getConnected()){
            if(sender.getOriginalCountry().getCode().equals(sender.getMaskedIp().substring(0,4))){
                return sender;
            }
            else{
                String countryName="";
                if (receiver.getMaskedIp().substring(0,4).equalsIgnoreCase("001")) {
                    countryName="IND";
                } else if (receiver.getMaskedIp().substring(0,4).equalsIgnoreCase("002")) {
                    countryName="USA";
                } else if (receiver.getMaskedIp().substring(0,4).equalsIgnoreCase("003")) {
                    countryName="AUS";
                } else if (receiver.getMaskedIp().substring(0,4).equalsIgnoreCase("004")) {
                    countryName="CHI";
                } else if (receiver.getMaskedIp().substring(0,4).equalsIgnoreCase("005")) {
                    countryName="JPN";
                }
                try {
                    sender = connect(senderId, countryName);
                    if (sender.getOriginalCountry().getCode().equals(sender.getMaskedIp().substring(0, 4))) {
                        return sender;
                    } else {
                        throw new Exception("Cannot establish communication");
                    }
                }catch (Exception e){
                    throw new Exception("Cannot establish communication");
                }
            }
        } else if (receiver.getConnected() == false) {
            if(sender.getOriginalCountry().getCountryName().toString().equals(receiver.getOriginalCountry().getCountryName().toString())){
                return sender;
            }
            else{
                 try{
                     sender=connect(senderId,receiver.getOriginalCountry().getCountryName().toString());
                     if(sender.getOriginalCountry().getCountryName().toString().equals(receiver.getOriginalCountry().getCountryName().toString())){
                         return sender;
                     }
                     else{
                         throw new Exception("Cannot establish communication");
                     }
                 }catch (Exception e){
                     throw new Exception("Cannot establish communication");
                 }
            }
        }
        return sender;
    }
}
