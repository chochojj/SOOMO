import styled from 'styled-components';
import { FiMenu } from 'react-icons/fi';
import LogoImage from '../assets/image/logo.png';

const HeaderCon = styled.header`
  width: 100%;
  min-width: 360px;
  height: 50px;
  background-color: ${(props) => props.theme.color.main_blue};
  box-shadow: 0px 0px 2px 2px rgba(0, 0, 0, 0.3);
  position: absolute;
  top: 0px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  .logo_img {
    width: 120px;
    margin-top: 10px;
    cursor: pointer;
  }
  .nav_icon {
    cursor: pointer;
    color: white;
  }
`;
const Header = ({ handleNav }) => {
  return (
    <HeaderCon>
      <img src={LogoImage} alt="로고" className="logo_img" />
      <FiMenu size={30} className="nav_icon" onClick={handleNav} />
    </HeaderCon>
  );
};

export default Header;
