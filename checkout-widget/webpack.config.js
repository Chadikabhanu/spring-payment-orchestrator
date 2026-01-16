const path = require('path');

module.exports = {
  entry: './src/sdk/PaymentGateway.js',
  mode: 'production',
  output: {
    filename: 'checkout.js',
    path: path.resolve(__dirname, 'dist'),
    library: {
      name: 'PaymentGateway',
      type: 'umd',
      export: 'default'
    },
    globalObject: 'this'
  },
};
