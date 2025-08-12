# In short

An ability to be implemented by an Agent who produces green hydrogen from green electricity.

# Details

The `GreenElectricityProducer` class offers very basic functionalities concerning green hydrogen production from green electricity that is procured via a power purchasing agreement (PPA) with variable renewable power plants.

# Available Products

* `PpaInformationRequest`: Request for Power Purchase Agreement (PPA) contract data with electricity production unit
* `PpaInformationForecastRequest`: Request for forecasted Power Purchase Agreement (PPA) contract data with electricity production unit

# Outputs

* `ConsumedElectricityInMWH`: Amount of electricity consumed in this period for operating the electrolysis unit
* `ReceivedMoneyForElectricityInEUR`: Total received money for selling electricity in EUR

# Messages

* [PointInTime](../Comms/PointInTime.md): `PpaInformationForecastRequest` or `PpaInformationRequest` sent to VariableRenewableOperator
