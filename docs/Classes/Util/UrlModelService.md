# In Short

`UrlModelService` helps to execute external models from within AMIRIS.
External models can be used, e.g., to define agent behaviour or optimise agent strategies.
The external model is addressed via a [POST web-request](https://en.wikipedia.org/wiki/POST_(HTTP)).

# Details

`UrlModelService` transforms a single input object into a JSON message using the [JSONable](./JSONable.md) interface.
The serialised message is sent to the external model.
It then waits for the response of the external model.
As soon as the external model returns a response in JSON format it is translated to a Java output class.
If no response is received before an optional timeout is reached, a RuntimeException is thrown.

## Usage

### Instantiation

UrlModelService is an abstract class that cannot be instantiated directly.
Instead, create an anonymous (child) class of UrlModelService like so:

```java
UrlModelService<RequestModel,ResponseModel> myService = new UrlModelService<RequestModel,ResponseModel>(urlString) {};
``` 

Note the brackets `{}` at the end of the line, which create an anonymous child class of UrlModelService.
Here, `RequestModel` and `ResponseModel` are Plain Old Java Objects (POJO) and `urlString` points to the url to call the API at.

Additional constructors are available that allow:

* specifying an optional timeout
* using a configuration via ParameterData matching the UrlModelService's `parameters` group.

### Request and Response data models

Each property name in `RequestModel` and `ResponseModel` must exactly match the name in the JSON interface of the external API.
In `RequestModel` each property requires a getter method following Java getter naming convention, i.e. "getMyPropertyName".
The `RequestModel` must implement the [JSONable](./JSONable.md) interface.
In `ResponseModel` each property requires a setter method following Java getter naming convention, i.e. "setMyPropertyName".

If any property in the Java data models does not exactly match its JSON correspondant, you need to declare that using an annotation **either** at the getter **or** the setter, like:

```java
@JsonProperty("name_of_property_in_JSON")
public String getMyProperty() {
    return this.myJavaProperty;
}
```

Note that for the `ResponseModel` each JSON property must have a Java equivalent.
If there is any JSON property that cannot be assigned to the Java model, the transformation will fail.

#### Example Request Model

```java
public class RequestModel implements JSONable {
  private final double a;
  private final double otherNameB;
  
  public RequestModel(double a, double otherNameB) {
    this.a = a;
    this.otherNameB = otherNameB;
  }
  
  public double getA() {
    return a;
  }
  
  @JsonProperty("b")
  public double getOtherNameB() {
    return otherNameB;
  }
}
```

#### Example Response Model

```java
public class ResponseModel {
  private double sum;
  
  public void setSum(double sum) {
    this.sum = sum;
  }
  
  public double getSum() {
    return sum;
  }
}
```

### Call

With the above instantiation, simply call

```java
ResponseModel response = myService.call(requestModel);
```

where `requestModel` is the prepared instance of your `RequestModel` to be sent to the external model.
You will receive `response`, an instance of `ResponseModel` and result of your query to the external API.

## External model

If the external model hasn't already got a POST web-request API, it can be easily created, e.g. with [FastAPI](https://fastapi.tiangolo.com/).
In the following a (very simple) example is provided to provide an idea how a web-API can be created.

### Installation

Install `fastapi` with your favourite python environment, e.g.

```
pip install fastapi[all]
```

Create and enter a folder called "fastapi".
Create a new, empty file named "__init__.py" in that folder.

### Api

Create the file "main.py" in the "fastapi" folder and paste:

```python
import uvicorn
from fastapi import FastAPI
from fastapi.responses import HTMLResponse
from sum import sum_implementation, SumResponse, SumTerms

app = FastAPI()


@app.get("/", response_class=HTMLResponse)
async def root():
    return """<html><body>Welcome to AMIRIS-API; Please check the <a href="/docs">documentation</a></body></html>"""


@app.post("/sum")
async def sum_api(terms: SumTerms) -> SumResponse:
    return sum_implementation(terms)


if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)  # noqa
```

When executed, this code initialises a very simple API on your pc using port 8000.
If you already have a running webserver on your machine, you might want to change the port.
At `localhost:8000/` a short text is shown linking to the automatically generated API documentation at `localhost:8000/documentation`.
At `localhost:8000/sum` the method `sum_implementation` is called using any input provided in `terms`, returning a `SumResponse` to the caller.

### Implementation

Create the file "sum.py" in that same folder with the following content:

```python
from pydantic import BaseModel


class SumTerms(BaseModel):
    """Input for sum API"""
    a: float
    b: float


class SumResponse(BaseModel):
    """Output from sum API"""
    sum: float


def sum_implementation(terms: SumTerms) -> SumResponse:
    """
    Sums two floating point values
    Args:
        terms: two values to be summed
    Returns: a+ b
    """
    result = terms.a + terms.b
    return SumResponse(sum=result)
```

This code defines:

* the class `SumTerms` which contains two floating point numbers to be summed.
* the class `SumResponse` containing a single floating point number to be returned.
* the `sum_implementation` function that takes a `SumTerms` input, and returns the sum of its terms `a` and `b`.

### Execution

To start your api and make it available to, e.g., AMIRIS agents, enter a Python-enables shell and navigate to your "fastapi" folder.
Then, enter `uvicorn main:app` or `python main.py` (equivalent).
You should see a message similar to `Uvicorn running on http://127.0.0.1:8000`.
You can see it is working by accessing `http://127.0.0.1:8000/` with your browser.

## Example: Calling sum API

### Main

Create and enter a new folder called "sum_caller".

### RequestModel

In that folder create the file "RequestModel.java" and enter

```java
public class RequestModel {
	private double a;
	private double b;

	public RequestModel(double a, double b) {
		this.a = a;
		this.b = b;
	}

	public double getA() {
		return a;
	}

	public double getB() {
		return b;
	}
}
```

This defines a plain old Java object with two members `a` and `b`, their getters and a constructor.
Mind that the names of all members here match those of the Python API class "SumTerms".
Also, mind that the getter names must match the Java convention for getters.

### ResponseModel

```java
public class ResponseModel {
	private double sum;

	public double getSum() {
		return sum;
	}

	public void setSum(double sum) {
		this.sum = sum;
	}
}
```

This defines a plain old Java object with one member `sum`.
Mind that the names of all members here match those of the Python API class "SumTerms".
Also, mind that the setter name must match the Java convention for setters.

### Caller

Create a new file named "Caller.java" and enter:

```java
public class Caller {
	private final UrlModelService<RequestModel, ResponseModel> microModel;

	public Caller(String serviceUrl) {
		microModel = new UrlModelService<RequestModel, ResponseModel>(serviceUrl) {};
	}

	public double callRemoteSum(double a, double b) {
		ResponseModel response = microModel.call(new RequestModel(a, b));
		return response.getSum();
	}
	
	public static void main(String[] args) {
		Caller agent = new Caller("http://127.0.0.1:8000/sum");
		double result = agent.callRemoteSum(1, 5);		
		System.out.println(result);
	}	
}
```

This creates a simple Caller that connects to the `/sum` endpoint of the previously defined Python POST api.
It requests to sum the numbers one and five and prints the result.

### Execution

Remember to start the Python API first and to leave it active while running your Java code.
Then, execute the "Caller" class, which should print `6` matching `1+5`.

# See Also

* [JSONable](./JSONable.md)